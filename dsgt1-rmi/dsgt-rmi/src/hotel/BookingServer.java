package hotel;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class BookingServer {
    public static void main(String[] args) {
        try {
            // Maak BookingManager instantie
            BookingManager manager = new BookingManager();

            // Exporteer BookingManager als remote object stub bevat poort en ip informatie.
            IBookingManager stub = (IBookingManager) UnicastRemoteObject.exportObject(manager, 1100);

            // Verbinden met RMI registry java tool
            Registry registry = LocateRegistry.getRegistry();
            // De stub binden aan de registery
            registry.rebind("BookingManager", stub);


            System.out.println("BookingServer ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}