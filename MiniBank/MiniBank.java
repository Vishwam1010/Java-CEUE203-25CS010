import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

record BankInfo(String name, String branch) {}

enum MenuOption {
    OPEN_ACCOUNT,
    DEPOSIT,
    WITHDRAW,
    TRANSFER,
    LIST_ACCOUNTS,
    REPORT,
    EXIT
}

enum TransactionType {
    DEPOSIT,
    WITHDRAW,
    TRANSFER
}

record Command(TransactionType type, String accountNumber, long amount) {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Id {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Positive {
    String message() default "must be > 0";
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface MaxLength {
    int value();
}

interface Transactable {
    void deposit(long amount) throws InvalidAmountException;
    boolean withdraw(long amount) throws InvalidAmountException, InsufficientFundsException;
}

interface InterestBearing {
    double interestRate();
    default double yearlyInterest() {
        return (interestRate() * getBalance()) / 100.0;
    }
    long getBalance();
}

@FunctionalInterface
interface WithdrawRule {
    boolean allow(Account account, long amount);
}

interface Premium {}

class BankException extends Exception {
    public BankException(String message) {
        super(message);
    }
}

class InvalidAmountException extends BankException {
    public InvalidAmountException(String message) {
        super(message);
    }
}

class AccountNotFoundException extends BankException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}

class InsufficientFundsException extends BankException {
    private final long shortfall;

    public InsufficientFundsException(String message, long shortfall) {
        super(message);
        this.shortfall = shortfall;
    }

    public long getShortfall() {
        return shortfall;
    }
}

abstract class Account implements java.io.Serializable, Comparable<Account>, Transactable, InterestBearing {
    private static long accountCounter = 1;

    @Id
    private final String accountNumber;
    private String ownerName;
    @Positive
    protected long balance;
    private boolean active;

    protected Account(String ownerName, long openingBalance) {
        this.accountNumber = "AC" + String.format("%04d", accountCounter++);
        this.ownerName = ownerName;
        this.balance = openingBalance;
        this.active = true;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public long getBalance() {
        return balance;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void deposit(long amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be positive.");
        }
        this.balance += amount;
    }

    @Override
    public boolean withdraw(long amount) throws InvalidAmountException, InsufficientFundsException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be positive.");
        }
        if (amount > balance) {
            long shortfall = amount - balance;
            throw new InsufficientFundsException("Insufficient funds. Shortfall: " + shortfall, shortfall);
        }
        this.balance -= amount;
        return true;
    }

    public void transfer(Account to, long amount) throws InvalidAmountException, InsufficientFundsException {
        if (to == null) {
            throw new InvalidAmountException("Destination account cannot be null.");
        }
        this.withdraw(amount);
        to.deposit(amount);
    }

    public abstract double interestRate();
    public abstract boolean canWithdraw(long amount);

    @Override
    public int compareTo(Account other) {
        return this.accountNumber.compareTo(other.accountNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return Objects.equals(accountNumber, account.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber);
    }

    @Override
    public String toString() {
        return accountNumber + " | " + ownerName + " | " + balance + " | active=" + active;
    }
}

class SavingsAccount extends Account {
    private final long minBalance;

    public SavingsAccount(String ownerName, long openingBalance) {
        super(ownerName, openingBalance);
        this.minBalance = 500L;
    }

    public SavingsAccount(String ownerName, long openingBalance, long minBalance) {
        super(ownerName, openingBalance);
        this.minBalance = minBalance;
    }

    @Override
    public double interestRate() {
        return 4.0;
    }

    @Override
    public boolean canWithdraw(long amount) {
        return (balance - amount) >= minBalance;
    }
}

class CurrentAccount extends Account {
    private final long overdraftLimit;

    public CurrentAccount(String ownerName, long openingBalance) {
        super(ownerName, openingBalance);
        this.overdraftLimit = 5000L;
    }

    public CurrentAccount(String ownerName, long openingBalance, long overdraftLimit) {
        super(ownerName, openingBalance);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public double interestRate() {
        return 0.0;
    }

    @Override
    public boolean canWithdraw(long amount) {
        return (balance - amount) >= -overdraftLimit;
    }
}

class FixedDepositAccount extends Account {
    public FixedDepositAccount(String ownerName, long openingBalance) {
        super(ownerName, openingBalance);
    }

    @Override
    public double interestRate() {
        return 7.0;
    }

    @Override
    public boolean canWithdraw(long amount) {
        return false;
    }
}

class Customer implements java.io.Serializable, Cloneable {
    private String name;
    private String email;
    private String mobile;
    private final String customerId;
    private Address address;

    private static long customerCounter = 101;

    public Customer(String name, String email, String mobile) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.customerId = "CUST" + customerCounter++;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Customer clone() {
        try {
            Customer cloned = (Customer) super.clone();
            if (this.address != null) {
                cloned.address = new Address(this.address.line, this.address.city, this.address.pincode);
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone failed", e);
        }
    }

    public static class Address implements java.io.Serializable {
        private final String line;
        private final String city;
        private final String pincode;

        public Address(String line, String city, String pincode) {
            this.line = line;
            this.city = city;
            this.pincode = pincode;
        }

        public String getLine() {
            return line;
        }

        public String getCity() {
            return city;
        }

        public String getPincode() {
            return pincode;
        }
    }
}

class Validator {
    private static final Pattern MOBILE = Pattern.compile("^[6-9][0-9]{9}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PAN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]$");
    private static final Pattern IFSC = Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$");
    private static final Pattern AMOUNT = Pattern.compile("^[1-9][0-9]*$");

    public static boolean isValidMobile(String value) {
        return value != null && MOBILE.matcher(value).matches();
    }

    public static boolean isValidEmail(String value) {
        return value != null && EMAIL.matcher(value).matches();
    }

    public static boolean isValidPan(String value) {
        return value != null && PAN.matcher(value).matches();
    }

    public static boolean isValidIfsc(String value) {
        return value != null && IFSC.matcher(value).matches();
    }

    public static boolean isValidAmount(String value) {
        return value != null && AMOUNT.matcher(value).matches();
    }
}

class CommandParser {
    public static Command parse(String line) {
        if (line == null || line.trim().isEmpty()) {
            throw new IllegalArgumentException("Command cannot be empty.");
        }
        String[] parts = line.trim().split("\\s+");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Command must have 3 parts: TYPE ACCOUNT AMOUNT");
        }
        TransactionType type = TransactionType.valueOf(parts[0].toUpperCase(Locale.ROOT));
        String accountNumber = parts[1];
        long amount = Long.parseLong(parts[2]);
        return new Command(type, accountNumber, amount);
    }
}

class StatementFormatter {
    public static String buildStatement(Account account) {
        if (account == null) {
            return "No account selected.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Account Statement\n");
        sb.append("----------------\n");
        sb.append("Number : ").append(account.getAccountNumber()).append("\n");
        sb.append("Owner  : ").append(account.getOwnerName()).append("\n");
        sb.append("Balance: ").append(account.getBalance()).append("\n");
        sb.append("Status : ").append(account.isActive() ? "Active" : "Inactive");
        return sb.toString();
    }
}

class AnnotationValidator {
    public static String[] validate(Object obj) {
        if (obj == null) {
            return new String[] {"Object is null."};
        }
        List<String> errors = new ArrayList<>();
        Class<?> type = obj.getClass();
        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    if (field.isAnnotationPresent(Positive.class)) {
                        if (value instanceof Number number) {
                            if (number.doubleValue() <= 0) {
                                errors.add(field.getName() + " " + field.getAnnotation(Positive.class).message());
                            }
                        }
                    }
                    if (field.isAnnotationPresent(MaxLength.class)) {
                        MaxLength maxLength = field.getAnnotation(MaxLength.class);
                        if (value instanceof String str && str.length() > maxLength.value()) {
                            errors.add(field.getName() + " exceeds max length " + maxLength.value());
                        }
                    }
                } catch (IllegalAccessException e) {
                    errors.add(field.getName() + " could not be validated.");
                }
            }
            type = type.getSuperclass();
        }
        return errors.toArray(new String[0]);
    }
}

class AccountStore {
    private final ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ArrayList<String> transactionHistory = new ArrayList<>();

    public void add(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null.");
        }
        accounts.put(account.getAccountNumber(), account);
        transactionHistory.add("ADDED " + account.getAccountNumber());
    }

    public Account get(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public List<Account> getAll() {
        return new ArrayList<>(accounts.values());
    }

    public List<Account> sortedByBalance() {
        ArrayList<Account> sorted = new ArrayList<>(accounts.values());
        sorted.sort(Comparator.comparingLong(Account::getBalance).reversed());
        return sorted;
    }

    public void addHistory(String entry) {
        transactionHistory.add(entry);
    }

    public ArrayList<String> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }
}

class TransactionProcessor {
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public void submit(Runnable task) {
        executorService.execute(task);
    }

    public void stop() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(20, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }

    public void simulateDeadlock() {
        Account a = new SavingsAccount("A", 5000L);
        Account b = new SavingsAccount("B", 5000L);

        Thread t1 = new Thread(() -> {
            synchronized (a) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
                synchronized (b) {
                    try {
                        a.transfer(b, 1000L);
                    } catch (InvalidAmountException | InsufficientFundsException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized (b) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
                synchronized (a) {
                    try {
                        b.transfer(a, 1000L);
                    } catch (InvalidAmountException | InsufficientFundsException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class StatePersister {
    public static void save(Account[] accounts, Path file) throws IOException {
        Files.createDirectories(file.getParent());
        try (OutputStream os = Files.newOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(accounts);
        }
    }

    public static Account[] load(Path file) throws IOException, ClassNotFoundException {
        if (!Files.exists(file)) {
            return new Account[0];
        }
        try (InputStream is = Files.newInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(is)) {
            Object obj = ois.readObject();
            if (obj instanceof Account[]) {
                return (Account[]) obj;
            }
        }
        return new Account[0];
    }
}

class TransactionLog {
    public static void append(Path file, String line) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file,
                line + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }
}

class ReportGenerator {
    public static String generate(Path logsDir, Path reportFile) throws IOException {
        Files.createDirectories(logsDir);
        long totalDeposits = 0L;
        long totalWithdrawals = 0L;
        long totalTransfers = 0L;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(logsDir, "*.log")) {
            for (Path path : stream) {
                if (Files.size(path) == 0) continue;
                List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                for (String line : lines) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length < 3) continue;
                    String action = parts[0].toUpperCase(Locale.ROOT);
                    long amount = Long.parseLong(parts[2]);
                    switch (action) {
                        case "DEPOSIT" -> totalDeposits += amount;
                        case "WITHDRAW" -> totalWithdrawals += amount;
                        case "TRANSFER" -> totalTransfers += amount;
                        default -> {}
                    }
                }
            }
        }

        String summary = "Total Deposits: " + totalDeposits + System.lineSeparator()
                + "Total Withdrawals: " + totalWithdrawals + System.lineSeparator()
                + "Total Transfers: " + totalTransfers + System.lineSeparator()
                + "Net Change: " + (totalDeposits - totalWithdrawals - totalTransfers);
        Files.writeString(reportFile, summary + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return summary;
    }
}

class Repository<T> {
    private final ConcurrentHashMap<String, T> store = new ConcurrentHashMap<>();

    public T save(T entity) {
        if (entity == null) return null;
        String id = resolveId(entity);
        store.put(id, entity);
        return entity;
    }

    public T findById(String id) {
        return store.get(id);
    }

    public ArrayList<T> findAll() {
        return new ArrayList<>(store.values());
    }

    public boolean delete(String id) {
        return store.remove(id) != null;
    }

    @SafeVarargs
    public final void saveAll(T... items) {
        for (T item : items) {
            if (item != null) {
                save(item);
            }
        }
    }

    public static <N extends Number> double average(N[] values) {
        if (values == null || values.length == 0) {
            return 0.0;
        }
        double total = 0.0;
        for (N value : values) {
            total += value.doubleValue();
        }
        return total / values.length;
    }

    private String resolveId(T entity) {
        Class<?> clazz = entity.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    try {
                        return String.valueOf(field.get(entity));
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Cannot read @Id value.", e);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw new IllegalStateException("Entity does not have an @Id field: " + entity.getClass().getSimpleName());
    }
}

class BankService {
    private final Repository<Account> repository;
    private final AccountStore accountStore;

    public BankService(Repository<Account> repository) {
        this.repository = repository;
        this.accountStore = new AccountStore();
        for (Account acc : repository.findAll()) {
            this.accountStore.add(acc);
        }
    }

    public Account openAccount(String ownerName, long openingBalance) throws InvalidAmountException {
        if (openingBalance < 0) {
            throw new InvalidAmountException("Opening balance cannot be negative.");
        }
        Account account = new SavingsAccount(ownerName, openingBalance, 500L);
        repository.save(account);
        accountStore.add(account);
        return account;
    }

    public void deposit(String accountNumber, long amount) throws BankException {
        Account account = repository.findById(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        account.deposit(amount);
        accountStore.addHistory("DEPOSIT " + accountNumber + " " + amount);
    }

    public void withdraw(String accountNumber, long amount) throws BankException {
        Account account = repository.findById(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        if (!account.canWithdraw(amount)) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal.", amount - account.getBalance());
        }
        account.withdraw(amount);
        accountStore.addHistory("WITHDRAW " + accountNumber + " " + amount);
    }

    public void transfer(String fromNumber, String toNumber, long amount) throws BankException {
        if (amount <= 0) {
            throw new InvalidAmountException("Transfer amount must be positive.");
        }
        Account from = repository.findById(fromNumber);
        Account to = repository.findById(toNumber);
        if (from == null) {
            throw new AccountNotFoundException("Source account not found: " + fromNumber);
        }
        if (to == null) {
            throw new AccountNotFoundException("Destination account not found: " + toNumber);
        }
        if (!from.canWithdraw(amount)) {
            throw new InsufficientFundsException("Transfer failed: insufficient funds.", amount - from.getBalance());
        }
        from.withdraw(amount);
        to.deposit(amount);
        accountStore.addHistory("TRANSFER " + fromNumber + " -> " + toNumber + " " + amount);
    }

    public ArrayList<Account> listAccounts() {
        return repository.findAll();
    }

    public String statement(String accountNumber) throws AccountNotFoundException {
        Account account = repository.findById(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        return StatementFormatter.buildStatement(account);
    }

    public AccountStore getAccountStore() {
        return accountStore;
    }
}

public class MiniBank {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final Path DATA_DIR = Paths.get("data");
    private static final Path ACCOUNTS_FILE = DATA_DIR.resolve("accounts.dat");
    private static final Path LOGS_DIR = DATA_DIR.resolve("logs");
    private static final Path REPORT_FILE = DATA_DIR.resolve("report.txt");

    public static void main(String[] args) throws Exception {
        BankInfo bankInfo = new BankInfo("CHARUSAT MiniBank", "Campus Branch");
        System.out.println("===========================================");
        System.out.println(bankInfo.name() + " | " + bankInfo.branch());
        System.out.println("===========================================");

        Repository<Account> repository = new Repository<>();
        loadAccounts(repository);

        BankService bankService = new BankService(repository);
        if (repository.findAll().isEmpty()) {
            seedSampleAccounts(bankService);
        }

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter choice: ");
            String input = SCANNER.nextLine().trim();

            try {
                int choice = Integer.parseInt(input);
                MenuOption option = switch (choice) {
                    case 1 -> MenuOption.OPEN_ACCOUNT;
                    case 2 -> MenuOption.DEPOSIT;
                    case 3 -> MenuOption.WITHDRAW;
                    case 4 -> MenuOption.TRANSFER;
                    case 5 -> MenuOption.LIST_ACCOUNTS;
                    case 6 -> MenuOption.REPORT;
                    case 7 -> MenuOption.EXIT;
                    default -> null;
                };

                if (option == null) {
                    System.out.println("Invalid option. Please choose a valid menu number.");
                    continue;
                }

                switch (option) {
                    case OPEN_ACCOUNT -> {
                        System.out.print("Owner name: ");
                        String owner = SCANNER.nextLine().trim();
                        System.out.print("Opening balance: ");
                        long openingBalance = Long.parseLong(SCANNER.nextLine().trim());
                        Account a = bankService.openAccount(owner, openingBalance);
                        System.out.println("Account opened: " + a.getAccountNumber());
                        TransactionLog.append(LOGS_DIR.resolve("transactions.log"), "DEPOSIT " + a.getAccountNumber() + " " + openingBalance);
                    }
                    case DEPOSIT -> {
                        System.out.print("Account number: ");
                        String acc = SCANNER.nextLine().trim();
                        System.out.print("Deposit amount: ");
                        long amount = Long.parseLong(SCANNER.nextLine().trim());
                        bankService.deposit(acc, amount);
                        System.out.println("Deposit successful.");
                        TransactionLog.append(LOGS_DIR.resolve("transactions.log"), "DEPOSIT " + acc + " " + amount);
                    }
                    case WITHDRAW -> {
                        System.out.print("Account number: ");
                        String acc = SCANNER.nextLine().trim();
                        System.out.print("Withdrawal amount: ");
                        long amount = Long.parseLong(SCANNER.nextLine().trim());
                        bankService.withdraw(acc, amount);
                        System.out.println("Withdrawal successful.");
                        TransactionLog.append(LOGS_DIR.resolve("transactions.log"), "WITHDRAW " + acc + " " + amount);
                    }
                    case TRANSFER -> {
                        System.out.print("From account: ");
                        String from = SCANNER.nextLine().trim();
                        System.out.print("To account: ");
                        String to = SCANNER.nextLine().trim();
                        System.out.print("Transfer amount: ");
                        long amount = Long.parseLong(SCANNER.nextLine().trim());
                        bankService.transfer(from, to, amount);
                        System.out.println("Transfer successful.");
                        TransactionLog.append(LOGS_DIR.resolve("transactions.log"), "TRANSFER " + from + " -> " + to + " " + amount);
                    }
                    case LIST_ACCOUNTS -> {
                        System.out.println("Account list:");
                        for (Account a : bankService.listAccounts()) {
                            System.out.println(a);
                        }
                    }
                    case REPORT -> {
                        String report = ReportGenerator.generate(LOGS_DIR, REPORT_FILE);
                        System.out.println(report);
                    }
                    case EXIT -> {
                        saveAccounts(repository);
                        System.out.println("Goodbye!");
                        running = false;
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number entered.");
            } catch (BankException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("File I/O error: " + e.getMessage());
            }
        }

        SCANNER.close();
    }

    private static void printMenu() {
        System.out.println("\nMiniBank Menu");
        System.out.println("1. Open Account");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Transfer");
        System.out.println("5. List Accounts");
        System.out.println("6. Generate Report");
        System.out.println("7. Exit");
    }

    private static void seedSampleAccounts(BankService service) throws InvalidAmountException {
        service.openAccount("Riya", 5000L);
        service.openAccount("Amit", 3000L);
        service.openAccount("Neha", 7000L);
    }

    private static void loadAccounts(Repository<Account> repository) throws IOException, ClassNotFoundException {
        Files.createDirectories(DATA_DIR);
        Account[] loaded = StatePersister.load(ACCOUNTS_FILE);
        for (Account account : loaded) {
            repository.save(account);
        }
    }

    private static void saveAccounts(Repository<Account> repository) throws IOException {
        Files.createDirectories(DATA_DIR);
        Account[] accounts = repository.findAll().toArray(new Account[0]);
        StatePersister.save(accounts, ACCOUNTS_FILE);
    }
}
