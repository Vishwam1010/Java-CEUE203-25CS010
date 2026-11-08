import java.util.LinkedList;
import java.util.Queue;

public class ProducerConsumer {
    private static final int ITEM_COUNT = 10;

    static class SharedBuffer {
        private final Queue<Integer> queue = new LinkedList<>();
        private final int capacity;

        SharedBuffer(int capacity) {
            this.capacity = capacity;
        }

        public synchronized void put(int item) throws InterruptedException {
            while (queue.size() == capacity) {
                wait();
            }

            queue.add(item);
            System.out.println("Produced: " + item);
            notifyAll();
        }

        public synchronized int take() throws InterruptedException {
            while (queue.isEmpty()) {
                wait();
            }

            int item = queue.remove();
            System.out.println("Consumed: " + item);
            notifyAll();
            return item;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SharedBuffer buffer = new SharedBuffer(3);

        Thread producer = new Thread(() -> {
            try {
                for (int item = 1; item <= ITEM_COUNT; item++) {
                    buffer.put(item);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Producer was interrupted.");
            }
        }, "Producer");

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= ITEM_COUNT; i++) {
                    buffer.take();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Consumer was interrupted.");
            }
        }, "Consumer");

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();

        System.out.println("Production and consumption are complete.");
    }
}