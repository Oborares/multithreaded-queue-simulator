package BusinessLogic;

import Model.Server;
import Model.Task;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public class ShortestQueueStrategy implements Strategy {
    @Override
    public void addTask(List<Server> servers, Task task) {
        Optional<Server> bestServer = servers.stream()
                .min((s1, s2) -> Integer.compare(s1.getWaitingQueue().size(), s2.getWaitingQueue().size()));

        bestServer.ifPresent(server -> {
            server.addTask(task);

        });

        if (bestServer.isEmpty()) {
            System.err.println("Warning: No server available to dispatch task " + task.getId());
        }
    }
}