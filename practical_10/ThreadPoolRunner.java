import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadPoolRunner {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        for (int taskId = 1; taskId <= 10; taskId++) {
            final int id = taskId;

            Runnable task = () -> {
                System.out.println("Task " + id + " started on "
                        + Thread.currentThread().getName());

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Task " + id + " was interrupted.");
                    return;
                }

                System.out.println("Task " + id + " finished on "
                        + Thread.currentThread().getName());
            };

            executor.submit(task);
        }

        executor.shutdown();

        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                System.out.println("Tasks did not finish in time; stopping the pool.");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            System.out.println("Main thread was interrupted.");
        }

        System.out.println("All tasks are complete.");
    }
}