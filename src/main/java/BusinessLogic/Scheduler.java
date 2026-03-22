package BusinessLogic;

import Model.Server;
import Model.Task;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {
    private final List<Server> servers = new ArrayList<>();
    private Strategy strategy;
    private final List<Thread> serverThreads = new ArrayList<>();

    public Scheduler(int numberOfQueues) {
        for (int i = 0; i < numberOfQueues; i++) {
            Server server = new Server(i + 1);
            servers.add(server);
            Thread serverThread = new Thread(server, "Model.Server-" + (i + 1));
            serverThreads.add(serverThread);
            serverThread.start();
        }

        this.changeStrategy(SelectionPolicy.SHORTEST_TIME);
        System.out.println("BusinessLogic.Scheduler initialized with " + numberOfQueues + " servers and SHORTEST_TIME strategy.");
    }
    public void changeStrategy(SelectionPolicy policy) {
        if (policy == SelectionPolicy.SHORTEST_QUEUE) {
            this.strategy = new ShortestQueueStrategy();
            System.out.println("BusinessLogic.Strategy changed to SHORTEST_QUEUE.");
        } else if (policy == SelectionPolicy.SHORTEST_TIME) {
            this.strategy = new ShortestTimeStrategy();
            System.out.println("BusinessLogic.Strategy changed to SHORTEST_TIME.");
        } else {
            System.err.println("Warning: Unknown selection policy specified. BusinessLogic.Strategy not changed.");
        }
    }

    public void dispatchTask(Task task) {
        if (strategy == null) {
            System.err.println("Error: BusinessLogic.Strategy not set in BusinessLogic.Scheduler.");
            return;
        }
        strategy.addTask(servers, task);
    }


    public List<Server> getServers() {
        return servers;
    }

    public void stopServers() {
        System.out.println("Attempting to stop server threads...");
        for (Server server : servers) {
            server.stop();
        }

         for (Thread t : serverThreads) {
             try {
                 t.join(2000);
             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
                 System.err.println("Interrupted while waiting for server threads to stop.");
             }
         }
        System.out.println("Model.Server stop signals sent.");
    }
}