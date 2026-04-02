package staff;

import hotel.IBookingManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.*;

public class ConnectionStormTest {
    public static void main(String[] args) throws Exception {
        String server = "lowie.switzerlandnorth.cloudapp.azure.com";

        System.out.println("=== CONNECTION STORM TEST ===");
        System.out.println("Creating connections without releasing them...\n");

        int maxConnections = 5000;

        for (int i = 1; i <= maxConnections; i++) {
            try {
                Registry registry = LocateRegistry.getRegistry(server);
                IBookingManager bm = (IBookingManager) registry.lookup("BookingManager");

                // Make one call to establish connection
                bm.getAllRooms();

                if (i % 100 == 0) {
                    System.out.println("Connections created: " + i);
                }

                // DON'T close - keep connections open

            } catch (Exception e) {
                System.out.println("\n❌ CONNECTION LIMIT REACHED at " + i + " connections");
                System.out.println("Error: " + e.getMessage());
                break;
            }
        }
    }
}