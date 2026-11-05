abstract class Employee {
    private final String name;
    private final String id;

    public Employee(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public abstract double monthlySalary();
}

class FullTime extends Employee {
    private final double fixedMonthlySalary;

    public FullTime(String name, String id, double fixedMonthlySalary) {
        super(name, id);
        this.fixedMonthlySalary = fixedMonthlySalary;
    }

    @Override
    public double monthlySalary() {
        return fixedMonthlySalary;
    }
}

class PartTime extends Employee {
    private final double hours;
    private final double hourlyRate;

    public PartTime(String name, String id, double hours, double hourlyRate) {
        super(name, id);
        this.hours = hours;
        this.hourlyRate = hourlyRate;
    }

    @Override
    public double monthlySalary() {
        return hours * hourlyRate;
    }
}

class Intern extends Employee {
    private final double stipend;

    public Intern(String name, String id, double stipend) {
        super(name, id);
        this.stipend = stipend;
    }

    @Override
    public double monthlySalary() {
        return stipend;
    }
}

public class Payroll {
    public static void main(String[] args) {
        Employee[] employees = {
            new FullTime("Asha Patel", "E101", 5000),
            new PartTime("Ravi Shah", "E102", 60, 25),
            new Intern("Mina Das", "E103", 1200),
            new FullTime("Noah Lee", "E104", 5500)
        };

        double totalPayroll = 0;

        for (Employee employee : employees) {
            double salary = employee.monthlySalary();
            totalPayroll += salary;

            System.out.printf("%s (ID: %s): $%.2f",
                    employee.getName(), employee.getId(), salary);

            if (employee instanceof Intern) {
                System.out.print(" — Intern stipend");
            }

            System.out.println();
        }

        System.out.printf("Total monthly payroll: $%.2f%n", totalPayroll);
    }
}