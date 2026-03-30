package staff;

import hotel.IBookingManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class LatencyTest {
    public static void main(String[] args) throws Exception {
        String server = "lowie.switzerlandnorth.cloudapp.azure.com";
        Registry registry = LocateRegistry.getRegistry(server);
        IBookingManager bm = (IBookingManager) registry.lookup("BookingManager");

        System.out.println("Testing latency from local VM to Azure...\n");

        // Warm-up (eerste calls zijn vaak trager)
        for (int i = 0; i < 5; i++) {
            bm.getAllRooms();
        }

        // Meet 50 requests
        long totalTime = 0;
        int iterations = 50;

        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            bm.getAllRooms();
            long end = System.currentTimeMillis();
            long duration = end - start;
            totalTime += duration;
            System.out.println("Request " + (i+1) + ": " + duration + " ms");
        }

        double average = totalTime / (double) iterations;
        System.out.println("\n=== Results ===");
        System.out.println("Total requests: " + iterations);
        System.out.println("Average latency: " + String.format("%.2f", average) + " ms");
    }
}