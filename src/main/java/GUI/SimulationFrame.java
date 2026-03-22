package GUI;

import BusinessLogic.SelectionPolicy;
import BusinessLogic.SimulationManager;
import Model.Server;
import Model.Task;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class SimulationFrame extends JFrame {
    private final JTextField clientsField = new JTextField("4");
    private final JTextField queuesField = new JTextField("2");
    private final JTextField simTimeField = new JTextField("60");
    private final JTextField arrivalMinField = new JTextField("2");
    private final JTextField arrivalMaxField = new JTextField("30");
    private final JTextField serviceMinField = new JTextField("2");
    private final JTextField serviceMaxField = new JTextField("4");
    private final JComboBox<String> strategyCombo = new JComboBox<>(new String[]{"Shortest Time", "Shortest Queue"});

    private final JButton startButton = new JButton("Start Simulation");
    private final JButton stopButton = new JButton("Stop Simulation");

    private final JPanel queuesPanel = new JPanel();
    private final JTextArea statusArea = new JTextArea(10, 40);
    private final JTextArea resultsArea = new JTextArea(10, 40);

    private SimulationManager simulationManager;
    private Thread simulationThread;


    public SimulationFrame() {
        super("Queues Management Application");
        setupUI();
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void setupUI() {
        setMinimumSize(new Dimension(800, 600));

        // Setup input panel
        JPanel inputPanel = new JPanel(new GridLayout(8, 2, 5, 5));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("Number of Clients (N):"));
        inputPanel.add(clientsField);

        inputPanel.add(new JLabel("Number of Queues (Q):"));
        inputPanel.add(queuesField);

        inputPanel.add(new JLabel("Simulation Time (seconds):"));
        inputPanel.add(simTimeField);

        inputPanel.add(new JLabel("Min Arrival Time:"));
        inputPanel.add(arrivalMinField);

        inputPanel.add(new JLabel("Max Arrival Time:"));
        inputPanel.add(arrivalMaxField);

        inputPanel.add(new JLabel("Min Service Time:"));
        inputPanel.add(serviceMinField);

        inputPanel.add(new JLabel("Max Service Time:"));
        inputPanel.add(serviceMaxField);

        inputPanel.add(new JLabel("Selection Strategy:"));
        inputPanel.add(strategyCombo);

        // Setup button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        stopButton.setEnabled(false);

        // Setup queues display panel
        queuesPanel.setLayout(new BoxLayout(queuesPanel, BoxLayout.Y_AXIS));
        queuesPanel.setBorder(BorderFactory.createTitledBorder("Queues Status"));
        JScrollPane queuesScrollPane = new JScrollPane(queuesPanel);
        queuesScrollPane.setPreferredSize(new Dimension(400, 200));

        // Setup status area
        statusArea.setEditable(false);
        JScrollPane statusScrollPane = new JScrollPane(statusArea);
        statusScrollPane.setBorder(BorderFactory.createTitledBorder("Status Log"));

        // Setup results area
        resultsArea.setEditable(false);
        JScrollPane resultsScrollPane = new JScrollPane(resultsArea);
        resultsScrollPane.setBorder(BorderFactory.createTitledBorder("Simulation Results"));

        // Layout components
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(inputPanel, BorderLayout.CENTER);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.add(queuesScrollPane, BorderLayout.NORTH);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.add(statusScrollPane);
        textPanel.add(resultsScrollPane);
        displayPanel.add(textPanel, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(controlPanel, BorderLayout.WEST);
        getContentPane().add(displayPanel, BorderLayout.CENTER);

        // Setup event handlers
        startButton.addActionListener(this::startSimulation);
        stopButton.addActionListener(e -> stopSimulation());
    }

    private void startSimulation(ActionEvent e) {
        try {
            int N = Integer.parseInt(clientsField.getText());
            int Q = Integer.parseInt(queuesField.getText());
            int simTime = Integer.parseInt(simTimeField.getText());
            int arrMin = Integer.parseInt(arrivalMinField.getText());
            int arrMax = Integer.parseInt(arrivalMaxField.getText());
            int servMin = Integer.parseInt(serviceMinField.getText());
            int servMax = Integer.parseInt(serviceMaxField.getText());

            if (N <= 0 || Q <= 0 || simTime <= 0) {
                JOptionPane.showMessageDialog(this, "Number of clients, queues, and simulation time must be positive.");
                return;
            }

            if (arrMax < arrMin || servMax < servMin) {
                JOptionPane.showMessageDialog(this, "Max values must be greater than or equal to min values.");
                return;
            }

            SelectionPolicy policy = strategyCombo.getSelectedIndex() == 0 ?
                    SelectionPolicy.SHORTEST_TIME :
                    SelectionPolicy.SHORTEST_QUEUE;

            // Create new simulation manager
            simulationManager = new SimulationManager(N, Q, simTime, arrMin, arrMax, servMin, servMax, policy);
            simulationManager.setGui(this);

            // Start simulation in a separate thread
            simulationThread = new Thread(simulationManager);
            simulationThread.start();

            // Update UI
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            statusArea.setText("Simulation started with parameters:\n" +
                    "N=" + N + ", Q=" + Q + ", SimTime=" + simTime + "\n" +
                    "Arrival Range=[" + arrMin + ", " + arrMax + "], Service Range=[" + servMin + ", " + servMax + "]\n" +
                    "Strategy=" + policy + "\n");

            // Initialize queues display
            queuesPanel.removeAll();
            for (int i = 0; i < Q; i++) {
                JPanel queuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                queuePanel.setBorder(BorderFactory.createTitledBorder("Queue " + (i + 1)));
                queuePanel.add(new JLabel("Empty"));
                queuesPanel.add(queuePanel);
            }
            queuesPanel.revalidate();
            queuesPanel.repaint();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers in all fields.");
        }
    }

    private void stopSimulation() {
        if (simulationManager != null) {
            simulationManager.stopSimulation();
        }

        if (simulationThread != null && simulationThread.isAlive()) {
            try {
                simulationThread.join(2000);
                if (simulationThread.isAlive()) {
                    simulationThread.interrupt();
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        statusArea.append("\nSimulation stopped manually.\n");
    }

    public void updateQueuesDisplay(List<Server> servers, List<Task> waitingTasks, int currentTime) {
        SwingUtilities.invokeLater(() -> {
            // Update status
            statusArea.append("Time " + currentTime + ": " + waitingTasks.size() + " tasks waiting.\n");

            // Update queues display
            queuesPanel.removeAll();

            // Add waiting clients panel
            JPanel waitingPanel = new JPanel();
            waitingPanel.setBorder(BorderFactory.createTitledBorder("Waiting Clients"));
            StringBuilder waitingStr = new StringBuilder();
            for (Task task : waitingTasks) {
                if (waitingStr.length() > 0) waitingStr.append(", ");
                waitingStr.append(task);
            }
            waitingPanel.add(new JLabel(waitingStr.length() > 0 ? waitingStr.toString() : "None"));
            queuesPanel.add(waitingPanel);

            // Add server queues
            for (Server server : servers) {
                JPanel queuePanel = new JPanel();
                queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.Y_AXIS));
                queuePanel.setBorder(BorderFactory.createTitledBorder("Queue " + server.getId()));

                // Current task
                Task currentTask = server.getCurrentTask();
                JLabel currentLabel = new JLabel("Current: " +
                        (currentTask != null ? currentTask + " (" + server.getRemainingServiceTime() + "s)" : "None"));
                currentLabel.setForeground(currentTask != null ? Color.RED : Color.BLACK);
                queuePanel.add(currentLabel);

                // Waiting tasks
                BlockingQueue<Task> queue = server.getWaitingQueue();
                StringBuilder queueStr = new StringBuilder("Waiting: ");
                if (queue.isEmpty()) {
                    queueStr.append("None");
                } else {
                    int i = 0;
                    for (Task task : queue) {
                        if (i > 0) queueStr.append(", ");
                        queueStr.append(task);
                        i++;
                    }
                }
                queuePanel.add(new JLabel(queueStr.toString()));

                // Wait time
                queuePanel.add(new JLabel("Total wait: " + server.getWaitingPeriod().get() + "s"));

                queuesPanel.add(queuePanel);
            }

            queuesPanel.revalidate();
            queuesPanel.repaint();
        });
    }

    public void simulationFinished(String results) {
        SwingUtilities.invokeLater(() -> {
            resultsArea.setText(results);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            statusArea.append("\nSimulation completed.\n");
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimulationFrame frame = new SimulationFrame();
            frame.setVisible(true);
        });
    }
}