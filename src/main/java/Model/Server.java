package Model;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {
    private final BlockingQueue<Task> waitingQueue = new LinkedBlockingQueue<>();
    private final AtomicInteger waitingPeriod = new AtomicInteger(0);
    private Task currentTask;
    private int remainingServiceTime;
    private volatile boolean isRunning = true;
    private final int id;

    public Server(int id) {
        this.id = id;
    }

    public void addTask(Task task) {
        waitingQueue.add(task);
        waitingPeriod.addAndGet(task.getServiceTime());
    }

    public void stop() {
        isRunning = false;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                currentTask = waitingQueue.poll(1, TimeUnit.SECONDS);
                if (currentTask != null) {
                    remainingServiceTime = currentTask.getServiceTime();
                    while (remainingServiceTime > 0 && isRunning) {
                        Thread.sleep(1000);
                        remainingServiceTime--;
                        waitingPeriod.decrementAndGet();
                    }
                    currentTask = null;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public BlockingQueue<Task> getWaitingQueue() { return waitingQueue; }
    public AtomicInteger getWaitingPeriod() { return waitingPeriod; }
    public int getId() { return id; }
    public Task getCurrentTask() { return currentTask; }
    public int getRemainingServiceTime() { return remainingServiceTime; }
}