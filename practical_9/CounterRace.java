import java.util.ArrayList;
import java.util.List;

public class CounterRace {
    private static final int THREAD_COUNT = 8;
    private static final int INCREMENTS_PER_THREAD = 500_000;

    private static class Counter {
        private int value = 0;

        // Deliberately unsafe: the read-modify-write is not atomic.
        void incrementUnsynchronized() {
            value++;
        }

        synchronized void incrementSynchronized() {
            value++;
        }

        int getValue() {
            return value;
        }
    }

    private static void runTest(boolean useSynchronization)
            throws InterruptedException {
        Counter counter = new Counter();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                    if (useSynchronization) {
                        counter.incrementSynchronized();
                    } else {
                        counter.incrementUnsynchronized();
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        int expected = THREAD_COUNT * INCREMENTS_PER_THREAD;
        System.out.println((useSynchronization ? "Synchronized" : "Unsynchronized")
                + " counter: " + counter.getValue()
                + " (expected " + expected + ")");
    }

    public static void main(String[] args) throws InterruptedException {
        runTest(false);
        runTest(true);
    }
}