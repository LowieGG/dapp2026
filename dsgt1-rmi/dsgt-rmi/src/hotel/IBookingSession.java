package hotel;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IBookingSession extends Remote {
    /**
     * Voeg een booking toe aan de shopping cart
     */
    void addBookingDetail(BookingDetail bookingDetail) throws RemoteException;

    /**
     * Boek alle items in de cart tegelijk.
     * Als 1 booking niet lukt, worden ALLE bookings geannuleerd.
     * @return List van succesvol geboekte items
     * @throws Exception als de transactie faalt
     */
    List<BookingDetail> bookAll() throws RemoteException, Exception;

    /**
     * Bekijk de huidige shopping cart
     */
    List<BookingDetail> getCart() throws RemoteException;

    /**
     * Leeg de shopping cart
     */
    void clearCart() throws RemoteException;
}