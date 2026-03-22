package Model;

public class Task {
    private final int id;
    private final int arrivalTime;
    private final int serviceTime;
    private int waitingTime;

    public Task(int id, int arrivalTime, int serviceTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.serviceTime = serviceTime;
        this.waitingTime = 0;
    }

    public int getArrivalTime() { return arrivalTime; }
    public int getServiceTime() { return serviceTime; }
    public int getId() { return id; }
    public int getWaitingTime() { return waitingTime; }
    public void incrementWaitingTime() { waitingTime++; }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", id, arrivalTime, serviceTime);
    }
}