package BusinessLogic;

import Model.Server;
import Model.Task;

import java.util.List;
import java.util.Optional;

public class ShortestTimeStrategy implements Strategy {
    @Override
    public void addTask(List<Server> servers, Task task) {
        Optional<Server> bestServer = servers.stream()
                .min((s1, s2) -> Integer.compare(s1.getWaitingPeriod().get(), s2.getWaitingPeriod().get()));


        bestServer.ifPresent(server -> {
            server.addTask(task);

        });

        if (bestServer.isEmpty()) {
            System.err.println("Warning: No server available to dispatch task " + task.getId());
        }
    }
}