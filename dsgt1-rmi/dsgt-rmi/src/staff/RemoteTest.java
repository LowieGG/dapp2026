package staff;

import hotel.IBookingManager;
import hotel.BookingDetail;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;

public class RemoteTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Connecting to Azure server...");

        Registry registry = LocateRegistry.getRegistry("lowie.switzerlandnorth.cloudapp.azure.com");
        IBookingManager bm = (IBookingManager) registry.lookup("BookingManager");

        System.out.println("Connected! Getting all rooms...");
        System.out.println("Rooms: " + bm.getAllRooms());

        System.out.println("\nTesting booking...");
        BookingDetail bd = new BookingDetail("TestFromLocal", 101, LocalDate.now());
        bm.addBooking(bd);
        System.out.println("✓ Booking successful from local VM to Azure!");
    }
}