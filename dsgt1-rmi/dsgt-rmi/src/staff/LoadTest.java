package staff;

import hotel.IBookingManager;
import hotel.BookingDetail;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.util.concurrent.*;

public class LoadTest {
    public static void main(String[] args) throws Exception {
        String server = "lowie.switzerlandnorth.cloudapp.azure.com";

        System.out.println("=== Progressive Load Test ===\n");

        // Test met 1, 5, 10, 20, 50 parallelle clients
        int[] clientCounts = {1, 5, 10, 20, 50};

        for (int numClients : clientCounts) {
            testWithClients(server, numClients);
            Thread.sleep(5000); // Pauze tussen tests
        }
    }

    private static void testWithClients(String server, int numClients) throws Exception {
        System.out.println("Testing with " + numClients + " concurrent clients...");

        ExecutorService executor = Executors.newFixedThreadPool(numClients);
        int requestsPerClient = 10;
        int successCount = 0;

        long startTime = System.currentTimeMillis();

        CountDownLatch latch = new CountDownLatch(numClients);

        for (int i = 0; i < numClients; i++) {
            final int clientId = i;
            executor.submit(() -> {
                try {
                    Registry registry = LocateRegistry.getRegistry(server);
                    IBookingManager bm = (IBookingManager) registry.lookup("BookingManager");

                    for (int j = 0; j < requestsPerClient; j++) {
                        bm.isRoomAvailable(101, LocalDate.now());
                    }
                } catch (Exception e) {
                    System.err.println("Client " + clientId + " error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long duration = System.currentTimeMillis() - startTime;

        executor.shutdown();

        double avgResponseTime = duration / (double)(numClients * requestsPerClient);

        System.out.println("  Duration: " + duration + " ms");
        System.out.println("  Average response time: " +
                String.format("%.2f", avgResponseTime) + " ms");
        System.out.println();
    }
}