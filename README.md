# 🚦 Multithreaded Queue Management Simulator

## 📌 Project Overview
A desktop application built in Java that simulates real-time queuing systems (like supermarket checkouts or server task processing). The application uses multithreading to manage concurrent clients arriving and being processed by multiple servers. It provides a live GUI to visualize the simulation and calculates performance metrics like average waiting time and peak hour traffic.

## 🛠️ Technical Stack & Concepts
* **Language:** Java
* **UI Framework:** Java Swing (with `SwingUtilities.invokeLater` for thread-safe UI updates)
* **Concurrency:** `Thread`, `Runnable`, `BlockingQueue`, `AtomicInteger`, `volatile` variables.
* **Design Patterns:** * **Strategy Pattern:** Used to dynamically switch between queue selection algorithms (`ShortestQueue` vs `ShortestTime`).
    * **MVC Architecture:** Clean separation between Model (Task/Server), View (SimulationFrame), and Controller/Logic (SimulationManager).

## ⚙️ Features
* **Real-Time Multithreading:** Each queue (Server) runs on its own independent thread, processing tasks asynchronously.
* **Dynamic Dispatching:** The Scheduler automatically routes new clients to the optimal queue based on the selected algorithm.
* **Live Dashboard:** A Swing-based GUI that updates every second, showing waiting clients, current server loads, and remaining service times.
* **Automated Logging:** Automatically exports a `.txt` log file at the end of the simulation detailing the step-by-step state of the queues and final analytical results.

## 🚀 How to Run
1. Clone the repository to your local machine.
2. Open the project in your preferred Java IDE (IntelliJ IDEA, Eclipse, etc.).
3. Run the `SimulationFrame` class to launch the GUI.
4. Input your desired simulation parameters (Clients, Queues, Time, etc.).
5. Click **Start Simulation** and watch the multithreaded routing in action!