package staff;

import hotel.IBookingManager;
import hotel.BookingDetail;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StressTest {
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger errorCount = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        String server = "lowie.switzerlandnorth.cloudapp.azure.com";

        System.out.println("=== STRESS TEST: Finding Breaking Point ===\n");

        // Progressive stress: 100, 200, 500, 1000, 2000 clients
        int[] clientCounts = {100, 200, 500, 1000, 2000};

        for (int numClients : clientCounts) {
            System.out.println("\n--- Testing with " + numClients + " concurrent clients ---");

            boolean success = stressTest(server, numClients);

            if (!success) {
                System.out.println("\n❌ BREAKING POINT FOUND at " + numClients + " clients!");
                break;
            }

            // Cool-down period
            System.out.println("Cooling down for 10 seconds...");
            Thread.sleep(10000);
        }

        System.out.println("\n=== Test Complete ===");
    }

    private static boolean stressTest(String server, int numClients) {
        successCount.set(0);
        errorCount.set(0);

        ExecutorService executor = Executors.newFixedThreadPool(numClients);
        int requestsPerClient = 50;
        long startTime = System.currentTimeMillis();

        CountDownLatch latch = new CountDownLatch(numClients);

        for (int i = 0; i < numClients; i++) {
            final int clientId = i;
            executor.submit(() -> {
                try {
                    Registry registry = LocateRegistry.getRegistry(server);
                    IBookingManager bm = (IBookingManager) registry.lookup("BookingManager");

                    for (int j = 0; j < requestsPerClient; j++) {
                        try {
                            bm.getAllRooms();
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            if (errorCount.get() == 1) {
                                System.err.println("First error from client " + clientId + ": " + e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Client " + clientId + " failed to connect: " + e.getMessage());
                    errorCount.addAndGet(requestsPerClient);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            boolean completed = latch.await(5, TimeUnit.MINUTES);

            if (!completed) {
                System.out.println("⚠ TIMEOUT: Not all clients completed in 5 minutes");
                executor.shutdownNow();
                return false;
            }

        } catch (InterruptedException e) {
            System.out.println("⚠ Test interrupted");
            executor.shutdownNow();
            return false;
        }

        executor.shutdown();

        long duration = System.currentTimeMillis() - startTime;
        int totalRequests = numClients * requestsPerClient;
        double successRate = (successCount.get() * 100.0) / totalRequests;
        double throughput = successCount.get() / (duration / 1000.0);

        System.out.println("Duration: " + duration + " ms (" + (duration/1000) + " seconds)");
        System.out.println("Successful: " + successCount.get() + " / " + totalRequests);
        System.out.println("Failed: " + errorCount.get());
        System.out.println("Success rate: " + String.format("%.1f", successRate) + "%");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " req/s");

        // Consider it a failure if success rate < 95%
        return successRate >= 95.0;
    }
}