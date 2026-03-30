package staff;

import hotel.IBookingManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThroughputTest {
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger errorCount = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        String server = "lowie.switzerlandnorth.cloudapp.azure.com";
        int numClients = 100; // Aantal parallelle clients
        int requestsPerClient = 200; // Requests per client

        System.out.println("=== Throughput Test ===");
        System.out.println("Clients: " + numClients);
        System.out.println("Requests per client: " + requestsPerClient);
        System.out.println("Total requests: " + (numClients * requestsPerClient));
        System.out.println("\nStarting test...\n");

        ExecutorService executor = Executors.newFixedThreadPool(numClients);
        long startTime = System.currentTimeMillis();

        // Start alle clients tegelijk
        for (int i = 0; i < numClients; i++) {
            final int clientId = i;
            executor.submit(() -> {
                try {
                    Registry registry = LocateRegistry.getRegistry(server);
                    IBookingManager bm = (IBookingManager) registry.lookup("BookingManager");

                    for (int j = 0; j < requestsPerClient; j++) {
                        bm.getAllRooms();
                        successCount.incrementAndGet();
                    }
                    System.out.println("Client " + clientId + " completed");
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("Client " + clientId + " error: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double durationSeconds = duration / 1000.0;
        double throughput = successCount.get() / durationSeconds;

        System.out.println("\n=== Results ===");
        System.out.println("Duration: " + duration + " ms (" +
                String.format("%.2f", durationSeconds) + " seconds)");
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + errorCount.get());
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " requests/second");
    }
}