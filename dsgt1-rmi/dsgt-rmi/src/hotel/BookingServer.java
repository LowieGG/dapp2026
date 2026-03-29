package hotel;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class BookingServer {
    public static void main(String[] args) {
        try {
            // Maak BookingManager instantie
            BookingManager manager = new BookingManager();

            // Exporteer als remote object
            IBookingManager stub = (IBookingManager) UnicastRemoteObject.exportObject(manager, 0);

            // Bind aan RMI registry
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("BookingManager", stub);

            System.out.println("BookingServer ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}