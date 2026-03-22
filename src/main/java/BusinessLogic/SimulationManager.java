package BusinessLogic;

import GUI.SimulationFrame;
import Model.Server;
import Model.Task;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SimulationManager implements Runnable {
    // Simulation parameters
    private final int numberOfClients;
    private final int numberOfQueues;
    private final int simulationTimeMax;
    private final int minArrivalTime;
    private final int maxArrivalTime;
    private final int minServiceTime;
    private final int maxServiceTime;

    // Core components
    private final Scheduler scheduler;
    private final List<Task> generatedTasks;
    private final List<Task> waitingTasks;

    // Simulation state
    private boolean running = false;
    private int currentTime = 0;
    private int peakHour = 0;
    private int maxClientsInSystem = 0;
    private double totalServiceTime = 0;

    // Logging
    private final StringBuilder log = new StringBuilder();
    private SimulationFrame gui; // Optional GUI reference

    public SimulationManager(int numberOfClients, int numberOfQueues, int simulationTimeMax,
                             int minArrivalTime, int maxArrivalTime,
                             int minServiceTime, int maxServiceTime,
                             SelectionPolicy policy) {
        this.numberOfClients = numberOfClients;
        this.numberOfQueues = numberOfQueues;
        this.simulationTimeMax = simulationTimeMax;
        this.minArrivalTime = minArrivalTime;
        this.maxArrivalTime = maxArrivalTime;
        this.minServiceTime = minServiceTime;
        this.maxServiceTime = maxServiceTime;


        this.scheduler = new Scheduler(numberOfQueues);
        this.scheduler.changeStrategy(policy);

        this.generatedTasks = generateRandomTasks();
        this.waitingTasks = new ArrayList<>(generatedTasks);

        logInitialParameters();
    }

    public void setGui(SimulationFrame gui) {
        this.gui = gui;
    }

    private List<Task> generateRandomTasks() {
        List<Task> tasks = new ArrayList<>();
        Random rand = new Random();

        System.out.println("Generating " + numberOfClients + " tasks...");

        for (int i = 0; i < numberOfClients; i++) {
            int arrivalTime = rand.nextInt(maxArrivalTime - minArrivalTime + 1) + minArrivalTime;
            int serviceTime = rand.nextInt(maxServiceTime - minServiceTime + 1) + minServiceTime;

            Task task = new Task(i + 1, arrivalTime, serviceTime);
            tasks.add(task);
            totalServiceTime += serviceTime;
        }

        // Sort tasks by arrival time
        Collections.sort(tasks, (t1, t2) -> Integer.compare(t1.getArrivalTime(), t2.getArrivalTime()));
        System.out.println("Tasks generated and sorted by arrival time.");
        return tasks;
    }

    @Override
    public void run() {
        System.out.println("Simulation starting...");
        running = true;
        currentTime = 0;

        try {
            while (running && currentTime <= simulationTimeMax) {
                log.append("\nTime ").append(currentTime).append("\n");

                // 1. Dispatch tasks that have arrived
                List<Task> arrivedTasks = new ArrayList<>();
                for (Task task : waitingTasks) {
                    if (task.getArrivalTime() <= currentTime) {
                        scheduler.dispatchTask(task);
                        arrivedTasks.add(task);
                    } else {
                        break; // Tasks are sorted, so we can break early
                    }
                }
                waitingTasks.removeAll(arrivedTasks);

                // 2. Log current state
                logCurrentState();

                // 3. Update peak hour statistics
                updatePeakHour();

                // 4. Update GUI if available
                if (gui != null) {
                    gui.updateQueuesDisplay(scheduler.getServers(), waitingTasks, currentTime);
                }

                // 5. Check if simulation is complete
                if (isSimulationComplete()) {
                    log.append("\n--- Simulation ended at time ").append(currentTime).append(" ---");
                    break;
                }

                // 6. Increment time and sleep
                currentTime++;
                Thread.sleep(1000); // 1 second per simulation step
            }
        } catch (InterruptedException e) {
            log.append("\n--- Simulation interrupted at time ").append(currentTime).append(" ---");
            Thread.currentThread().interrupt();
        } finally {
            finishSimulation();
        }
    }

    private boolean isSimulationComplete() {
        if (waitingTasks.isEmpty()) {
            boolean allServersIdle = true;
            for (Server server : scheduler.getServers()) {
                if (server.getCurrentTask() != null || !server.getWaitingQueue().isEmpty()) {
                    allServersIdle = false;
                    break;
                }
            }
            return allServersIdle;
        }
        return false;
    }

    private void finishSimulation() {
        running = false;
        scheduler.stopServers();
        calculateAndLogResults();
        writeLogToFile();
        System.out.println("Simulation finished.");

        if (gui != null) {
            gui.simulationFinished(getResults());
        }
    }

    private void logInitialParameters() {
        log.append("Simulation Parameters:\n");
        log.append(String.format(" Clients: %d, Queues: %d, Max Time: %d\n",
                numberOfClients, numberOfQueues, simulationTimeMax));
        log.append(String.format(" Arrival Range: [%d, %d], Service Range: [%d, %d]\n",
                minArrivalTime, maxArrivalTime, minServiceTime, maxServiceTime));
        log.append("------------------------------------\n");
    }

    private void logCurrentState() {
        // Log waiting clients
        String waitingClientsStr = waitingTasks.stream()
                .map(Task::toString)
                .collect(Collectors.joining("; "));
        log.append("Waiting clients: [").append(waitingClientsStr).append("]\n");

        // Log queues state
        List<Server> servers = scheduler.getServers();
        for (Server server : servers) {
            String queueContent = server.getWaitingQueue().stream()
                    .map(Task::toString)
                    .collect(Collectors.joining("; "));

            Task currentTask = server.getCurrentTask();
            String status;

            if (currentTask != null) {
                status = String.format("Processing %s, remaining: %ds",
                        currentTask.toString(), server.getRemainingServiceTime());

                // Increment waiting time for tasks in queue
                for (Task task : server.getWaitingQueue()) {
                    task.incrementWaitingTime();
                }
            } else {
                status = "Idle";
            }

            log.append(String.format("Queue %d [%s]: {%s} | Load: %d\n",
                    server.getId(), status, queueContent, server.getWaitingPeriod().get()));
        }
    }



    private void updatePeakHour() {
        int currentClientsInQueues = 0;
        List<Server> servers = scheduler.getServers();

        for (Server server : servers) {
            currentClientsInQueues += server.getWaitingQueue().size();
            if (server.getCurrentTask() != null) {
                currentClientsInQueues++;
            }
        }

        if (currentClientsInQueues > maxClientsInSystem) {
            maxClientsInSystem = currentClientsInQueues;
            peakHour = currentTime;
        }
    }
    private void calculateAndLogResults() {
        log.append("\n------------------------------------\n");
        log.append("Simulation Results:\n");

        double totalWaitingTime = 0;
        int completedTasks = 0;

        for (Task task : generatedTasks) {
            if (task.getWaitingTime() > 0) {
                totalWaitingTime += task.getWaitingTime();
                completedTasks++;
            }
        }

        double avgWaitingTime = completedTasks > 0 ? totalWaitingTime / completedTasks : 0;
        double avgServiceTime = numberOfClients > 0 ? totalServiceTime / numberOfClients : 0;

        log.append(String.format("- Average Waiting Time: %.2f seconds\n", avgWaitingTime));
        log.append(String.format("- Average Service Time: %.2f seconds\n", avgServiceTime));
        log.append(String.format("- Peak Hour: Time %d (with %d clients in system)\n",
                peakHour, maxClientsInSystem));

        log.append("------------------------------------\n");
    }

    private void writeLogToFile() {
        String filename = "simulation_log_" + System.currentTimeMillis() + ".txt";
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(log.toString());
            System.out.println("Log written to " + filename);
        } catch (IOException e) {
            System.err.println("Error writing log file: " + e.getMessage());
        }
    }

    public String getResults() {
        return log.toString();
    }

    public void stopSimulation() {
        running = false;
    }

    public static void main(String[] args) {
        // Example usage with test case 1
        int N = 4, Q = 2, simTime = 60;
        int arrMin = 2, arrMax = 30;
        int servMin = 2, servMax = 4;

        SimulationManager manager = new SimulationManager(N, Q, simTime,
                arrMin, arrMax, servMin, servMax,
                SelectionPolicy.SHORTEST_TIME);

    }
}