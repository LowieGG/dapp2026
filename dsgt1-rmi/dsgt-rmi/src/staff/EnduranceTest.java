package staff;

import hotel.IBookingManager;
import hotel.BookingDetail;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;  // ← VOEG DEZE TOE


public class EnduranceTest {
    public static void main(String[] args) throws Exception {
        String server = "lowie.switzerlandnorth.cloudapp.azure.com";

        System.out.println("=== ENDURANCE TEST: 30 minutes sustained load ===");
        System.out.println("Monitor server CPU/Memory with: ssh + htop\n");

        Registry registry = LocateRegistry.getRegistry(server);
        IBookingManager bm = (IBookingManager) registry.lookup("BookingManager");

        int numThreads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        long startTime = System.currentTimeMillis();
        long duration = 30 * 60 * 1000; // 30 minutes

        AtomicInteger requestCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Statistics printer thread
        Thread statsThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(10000); // Every 10 seconds
                    long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                    System.out.println("[" + elapsed + "s] Requests: " + requestCount.get() +
                            ", Errors: " + errorCount.get());
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        statsThread.start();

        // Worker threads
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                while (System.currentTimeMillis() - startTime < duration) {
                    try {
                        bm.getAllRooms();
                        requestCount.incrementAndGet();

                        // Small delay to simulate realistic usage
                        Thread.sleep(100);

                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(35, TimeUnit.MINUTES);
        statsThread.interrupt();

        System.out.println("\n=== Endurance Test Complete ===");
        System.out.println("Total requests: " + requestCount.get());
        System.out.println("Total errors: " + errorCount.get());
        System.out.println("Success rate: " + (100.0 * (requestCount.get() - errorCount.get()) / requestCount.get()) + "%");
    }
}