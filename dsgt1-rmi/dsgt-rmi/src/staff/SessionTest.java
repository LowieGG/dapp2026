package staff;

import hotel.BookingDetail;
import hotel.IBookingManager;
import hotel.IBookingSession;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;

public class SessionTest {

    public static void main(String[] args) throws Exception {
        // Connect to server
        Registry registry = LocateRegistry.getRegistry("localhost");
        IBookingManager bm = (IBookingManager) registry.lookup("BookingManager");

        LocalDate today = LocalDate.now();

        System.out.println("=== Test 1: Successful transaction (all rooms available) ===\n");
        testSuccessfulTransaction(bm, today);

        Thread.sleep(2000);

        System.out.println("\n=== Test 2: Failed transaction (one room not available) ===\n");
        testFailedTransaction(bm, today.plusDays(1));
    }

    private static void testSuccessfulTransaction(IBookingManager bm, LocalDate date) throws Exception {
        // Maak een nieuwe sessie
        IBookingSession session = bm.createSession();

        // Voeg 3 bookings toe aan shopping cart
        System.out.println("Adding bookings to cart...");
        session.addBookingDetail(new BookingDetail("Alice", 101, date));
        session.addBookingDetail(new BookingDetail("Bob", 102, date));
        session.addBookingDetail(new BookingDetail("Carol", 201, date));

        System.out.println("\nCart contains " + session.getCart().size() + " items");

        // Boek alles tegelijk
        System.out.println("\nAttempting to book all...");
        try {
            session.bookAll();
            System.out.println("✓ SUCCESS: All bookings completed!\n");
        } catch (Exception e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
        }
    }

    private static void testFailedTransaction(IBookingManager bm, LocalDate date) throws Exception {
        // Boek eerst kamer 102 (zodat die niet meer beschikbaar is)
        System.out.println("Pre-booking room 102 for David...");
        bm.addBooking(new BookingDetail("David", 102, date));
        System.out.println("Room 102 is now occupied\n");

        // Maak een nieuwe sessie
        IBookingSession session = bm.createSession();

        // Voeg 3 bookings toe, waarvan 1 zal falen (102)
        System.out.println("Adding bookings to cart (including unavailable room 102)...");
        session.addBookingDetail(new BookingDetail("Eve", 101, date));
        session.addBookingDetail(new BookingDetail("Frank", 102, date)); // Deze zal falen!
        session.addBookingDetail(new BookingDetail("Grace", 203, date));

        System.out.println("\nCart contains " + session.getCart().size() + " items");

        // Probeer alles te boeken (dit zou moeten falen en rollback doen)
        System.out.println("\nAttempting to book all...");
        try {
            session.bookAll();
            System.out.println("✓ SUCCESS: All bookings completed!\n");
        } catch (Exception e) {
            System.out.println("✗ FAILED: " + e.getMessage());
            System.out.println("No bookings were made (transaction rolled back)\n");
        }

        // Verifieer dat kamer 101 en 203 nog steeds beschikbaar zijn
        System.out.println("Verification:");
        System.out.println("Room 101 available? " + bm.isRoomAvailable(101, date));
        System.out.println("Room 203 available? " + bm.isRoomAvailable(203, date));
    }
}