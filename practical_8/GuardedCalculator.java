import java.util.Scanner;

class DivideByZeroException extends Exception {
    public DivideByZeroException() {
        super("Cannot divide by zero.");
    }
}

public class GuardedCalculator {
    public static void main(String[] args) {
        int attempt = 0;
        boolean calculationSucceeded = false;

        try (Scanner scanner = new Scanner(System.in)) {
            while (!calculationSucceeded) {
                attempt++;

                try {
                    System.out.println("\nAttempt " + attempt);

                    System.out.print("Enter the first number: ");
                    double firstNumber =
                            Double.parseDouble(scanner.nextLine().trim());

                    System.out.print("Enter an operator (+, -, *, /): ");
                    String operator = scanner.nextLine().trim();

                    System.out.print("Enter the second number: ");
                    double secondNumber =
                            Double.parseDouble(scanner.nextLine().trim());

                    double result = switch (operator) {
                        case "+" -> firstNumber + secondNumber;
                        case "-" -> firstNumber - secondNumber;
                        case "*" -> firstNumber * secondNumber;
                        case "/" -> {
                            if (secondNumber == 0) {
                                throw new DivideByZeroException();
                            }
                            yield firstNumber / secondNumber;
                        }
                        default -> throw new IllegalArgumentException(
                                "Invalid operator. Use +, -, *, or /.");
                    };

                    System.out.println("Result: " + result);
                    calculationSucceeded = true;

                } catch (NumberFormatException e) {
                    System.out.println(
                            "Invalid number. Please enter numeric values.");
                } catch (DivideByZeroException e) {
                    System.out.println(e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                } finally {
                    System.out.println("Attempt " + attempt + " logged.");
                }
            }
        }
    }
}