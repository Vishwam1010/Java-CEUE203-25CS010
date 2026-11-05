public class CinemaShow {
    private final String title;
    private int seatsAvailable;
    private final int capacity;
    private static int totalBooked = 0;

    public CinemaShow(String title, int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative.");
        }

        this.title = title;
        this.capacity = capacity;
        this.seatsAvailable = capacity;
    }

    public CinemaShow(String title) {
        this(title, 100);
    }

    public boolean book(int n) {
        if (n <= 0 || n > seatsAvailable) {
            return false;
        }

        seatsAvailable -= n;
        totalBooked += n;
        return true;
    }

    public void cancel(int n) {
        if (n <= 0) {
            return;
        }

        seatsAvailable = Math.min(capacity, seatsAvailable + n);
    }

    public String getTitle() {
        return title;
    }

    public int getSeatsAvailable() {
        return seatsAvailable;
    }

    public static int getTotalBooked() {
        return totalBooked;
    }

    public static void main(String[] args) {
        CinemaShow show = new CinemaShow("Evening Show", 50);

        printBooking(show, 20);
        printBooking(show, 40);
        printBooking(show, 10);

        show.cancel(5);
        System.out.println(show.getTitle() + ": cancelled 5 seats. Seats available: "
                + show.getSeatsAvailable());

        printBooking(show, 25);

        System.out.println("Total booked: " + CinemaShow.getTotalBooked());
    }

    private static void printBooking(CinemaShow show, int numberOfSeats) {
        boolean successful = show.book(numberOfSeats);
        System.out.println(show.getTitle() + ": booking " + numberOfSeats
                + " seats: " + successful + ". Seats available: "
                + show.getSeatsAvailable());
    }
}