package staff;

import hotel.BookingDetail;
import hotel.IBookingManager;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConcurrentBookingTest {

    public static void main(String[] args) throws Exception {
        // Connect to server
        Registry registry = LocateRegistry.getRegistry("localhost");
        IBookingManager bm = (IBookingManager) registry.lookup("BookingManager");

        LocalDate today = LocalDate.now();

        System.out.println("=== Testing concurrent bookings for SAME room ===");
        testSameRoom(bm, today);

        Thread.sleep(2000);

        System.out.println("\n=== Testing concurrent bookings for DIFFERENT rooms ===");
        testDifferentRooms(bm, today.plusDays(1));
    }

    private static void testSameRoom(IBookingManager bm, LocalDate date) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // 3 clients proberen tegelijk kamer 101 te boeken
        for (int i = 1; i <= 3; i++) {
            final int clientId = i;
            executor.submit(() -> {
                try {
                    BookingDetail bd = new BookingDetail("Client" + clientId, 101, date);
                    bm.addBooking(bd);
                    System.out.println("✓ Client" + clientId + " succeeded!");
                } catch (Exception e) {
                    System.out.println("✗ Client" + clientId + " failed: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    private static void testDifferentRooms(IBookingManager bm, LocalDate date) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // 4 clients boeken elk een verschillende kamer (parallel!)
        int[] rooms = {101, 102, 201, 203};
        for (int i = 0; i < 4; i++) {
            final int clientId = i + 1;
            final int roomNumber = rooms[i];
            executor.submit(() -> {
                try {
                    BookingDetail bd = new BookingDetail("Client" + clientId, roomNumber, date);
                    long start = System.currentTimeMillis();
                    bm.addBooking(bd);
                    long duration = System.currentTimeMillis() - start;
                    System.out.println("✓ Client" + clientId + " booked room " + roomNumber +
                            " in " + duration + "ms");
                } catch (Exception e) {
                    System.out.println("✗ Client" + clientId + " failed: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
}