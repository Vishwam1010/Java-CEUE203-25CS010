abstract class MediaItem {
    private final String title;
    private final int daysLate;

    public MediaItem(String title, int daysLate) {
        this.title = title;
        this.daysLate = Math.max(daysLate, 0);
    }

    public String getTitle() {
        return title;
    }

    public int getDaysLate() {
        return daysLate;
    }

    public abstract double lateFee();
}

class Book extends MediaItem {
    private static final double DAILY_FEE = 0.50;

    public Book(String title, int daysLate) {
        super(title, daysLate);
    }

    @Override
    public double lateFee() {
        return getDaysLate() * DAILY_FEE;
    }
}

class DVD extends MediaItem {
    private static final double DAILY_FEE = 1.50;

    public DVD(String title, int daysLate) {
        super(title, daysLate);
    }

    @Override
    public double lateFee() {
        return getDaysLate() * DAILY_FEE;
    }
}

class Magazine extends MediaItem {
    private static final double DAILY_FEE = 0.25;

    public Magazine(String title, int daysLate) {
        super(title, daysLate);
    }

    @Override
    public double lateFee() {
        return getDaysLate() * DAILY_FEE;
    }
}

public class MediaLateFees {
    public static void main(String[] args) {
        MediaItem[] returnedItems = {
            new Book("The Hobbit", 4),
            new DVD("Finding Nemo", 3),
            new Magazine("Science Weekly", 6),
            new Book("Pride and Prejudice", 0)
        };

        double totalFees = 0;

        for (MediaItem item : returnedItems) {
            double fee = item.lateFee();
            totalFees += fee;

            System.out.printf("%s (%d days late): $%.2f%n",
                    item.getTitle(), item.getDaysLate(), fee);
        }

        System.out.printf("Total late fees: $%.2f%n", totalFees);
    }
}