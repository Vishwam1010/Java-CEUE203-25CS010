import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class SeatBookingRace {
    private static final int THREAD_COUNT = 10;
    private static final int INITIAL_SEATS = 5;

    private static class BookingSystem {
        private int seatsLeft = INITIAL_SEATS;
        private final AtomicInteger successfulBookings = new AtomicInteger(0);
        private final CountDownLatch checkedLatch =
                new CountDownLatch(THREAD_COUNT);
        private final CountDownLatch startLatch = new CountDownLatch(1);

        // Deliberately unsafe: threads check availability before any decrement.
        boolean bookUnsynchronized() throws InterruptedException {
            boolean sawAvailableSeat = seatsLeft > 0;

            checkedLatch.countDown();
            startLatch.await();

            if (sawAvailableSeat) {
                seatsLeft--;
                successfulBookings.incrementAndGet();
                return true;
            }
            return false;
        }

        synchronized boolean bookSynchronized() {
            if (seatsLeft > 0) {
                seatsLeft--;
                successfulBookings.incrementAndGet();
                return true;
            }
            return false;
        }

        int getSeatsLeft() {
            return seatsLeft;
        }

        int getSuccessfulBookings() {
            return successfulBookings.get();
        }
    }

    private static void runTest(boolean useSynchronization)
            throws InterruptedException {
        BookingSystem system = new BookingSystem();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread thread = new Thread(() -> {
                try {
                    boolean booked;

                    if (useSynchronization) {
                        booked = system.bookSynchronized();
                    } else {
                        booked = system.bookUnsynchronized();
                    }

                    if (booked) {
                        System.out.println(Thread.currentThread().getName()
                                + " booked a seat.");
                    } else {
                        System.out.println(Thread.currentThread().getName()
                                + " could not book; no seats left.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println(Thread.currentThread().getName()
                            + " was interrupted.");
                }
            }, "Customer-" + (i + 1));

            threads.add(thread);
            thread.start();
        }

        if (!useSynchronization) {
            // Wait until every thread has checked availability, then release them.
            system.checkedLatch.await();
            system.startLatch.countDown();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println((useSynchronization ? "Synchronized" : "Unsynchronized")
                + " booking: " + system.getSuccessfulBookings()
                + " successful, " + system.getSeatsLeft() + " seats left.");
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Without synchronization ===");
        runTest(false);

        System.out.println("\n=== With synchronized book() ===");
        runTest(true);
    }
}