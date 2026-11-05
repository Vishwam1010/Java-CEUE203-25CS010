import java.util.Scanner;

public class Tollbooth {
    record Vehicle(String number, String type) {}

    public static void main(String[] args) {
        int totalToll = 0;
        int bikeCount = 0;
        int carCount = 0;
        int truckCount = 0;

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("Enter vehicle number (or done to finish): ");
                String number = scanner.nextLine().trim();

                if (number.equalsIgnoreCase("done")) {
                    break;
                }

                System.out.print("Enter vehicle type (bike, car, truck): ");
                String type = scanner.nextLine().trim().toLowerCase();

                Vehicle vehicle = new Vehicle(number, type);

                int toll = switch (vehicle.type()) {
                    case "bike" -> 20;
                    case "car" -> 50;
                    case "truck" -> 150;
                    default -> {
                        System.out.println("Invalid type. Enter bike, car, or truck.");
                        yield 0;
                    }
                };

                if (toll == 0) {
                    continue;
                }

                totalToll += toll;

                switch (vehicle.type()) {
                    case "bike" -> bikeCount++;
                    case "car" -> carCount++;
                    case "truck" -> truckCount++;
                    default -> {
                        // The type was already checked above.
                    }
                }
            }
        }

        System.out.println("Total toll: " + totalToll);

        if (bikeCount == 0 && carCount == 0 && truckCount == 0) {
            System.out.println("Most frequent: none");
        } else if (carCount >= bikeCount && carCount >= truckCount) {
            System.out.println("Most frequent: car");
        } else if (bikeCount >= truckCount) {
            System.out.println("Most frequent: bike");
        } else {
            System.out.println("Most frequent: truck");
        }
    }
}