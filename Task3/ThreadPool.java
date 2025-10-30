import java.util.LinkedList;
import java.util.List;

public class ThreadPool {
    private final int capacity;
    private final List<Worker> workers;
    private final LinkedList<Runnable> taskQueue = new LinkedList<>();
    private volatile boolean isShutdown = false;

    public ThreadPool(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be > 0");
        this.capacity = capacity;
        this.workers = new LinkedList<>();
        for (int i = 0; i < capacity; i++) {
            Worker worker = new Worker();
            workers.add(worker);
            worker.start();
        }
    }

    public void execute(Runnable task) {
        synchronized (taskQueue) {
            if (isShutdown) {
                throw new IllegalStateException("ThreadPool is shut down");
            }
            taskQueue.add(task);
            taskQueue.notify();
        }
    }

    public void shutdown() {
        isShutdown = true;
        synchronized (taskQueue) {
            taskQueue.notifyAll();
        }
    }

    public void awaitTermination() {
        for (Worker worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public int getCapacity() {
        return capacity;
    }

    public static void main(String[] args) {
        ThreadPool pool = new ThreadPool(4);
        System.out.println("ThreadPool capacity: " + pool.getCapacity());
        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            pool.execute(() -> {
                String threadName = Thread.currentThread().getName();
                System.out.println("Task #" + taskId + " is running in " + threadName);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
            });
        }
        pool.shutdown();
        pool.awaitTermination();
        System.out.println("All tasks finished");
    }

    private class Worker extends Thread {
        @Override
        public void run() {
            while (true) {
                Runnable task;
                synchronized (taskQueue) {
                    while (taskQueue.isEmpty() && !isShutdown) {
                        try {
                            taskQueue.wait();
                        } catch (InterruptedException ignored) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    if (taskQueue.isEmpty() && isShutdown) {
                        break;
                    }
                    task = taskQueue.removeFirst();
                }
                try {
                    task.run();
                } catch (RuntimeException e) {
                     System.out.println("Error executing task: " + e.getStackTrace());
                }
            }
        }
    }
}
