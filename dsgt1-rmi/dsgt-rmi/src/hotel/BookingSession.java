package hotel;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class BookingSession implements IBookingSession {
    private List<BookingDetail> cart;
    private BookingManager bookingManager;

    public BookingSession(BookingManager bookingManager) {
        this.cart = new ArrayList<>();
        this.bookingManager = bookingManager;
    }

    @Override
    public void addBookingDetail(BookingDetail bookingDetail) throws RemoteException {
        cart.add(bookingDetail);
        System.out.println("Added to cart: " + bookingDetail.getGuest() +
                " - Room " + bookingDetail.getRoomNumber() +
                " on " + bookingDetail.getDate());
    }

    @Override
    public List<BookingDetail> bookAll() throws RemoteException, Exception {
        if (cart.isEmpty()) {
            throw new Exception("Shopping cart is empty");
        }

        System.out.println("\n=== Starting transaction for " + cart.size() + " bookings ===");

        // Fase 1: Check of alle bookings mogelijk zijn
        for (BookingDetail bd : cart) {
            if (!bookingManager.isRoomAvailable(bd.getRoomNumber(), bd.getDate())) {
                System.out.println("Transaction FAILED: Room " + bd.getRoomNumber() +
                        " not available on " + bd.getDate());
                throw new Exception("Transaction failed: Room " + bd.getRoomNumber() +
                        " is not available on " + bd.getDate());
            }
        }

        // Fase 2: Alle bookings zijn mogelijk, nu echt boeken
        List<BookingDetail> booked = new ArrayList<>();
        try {
            for (BookingDetail bd : cart) {
                bookingManager.addBooking(bd);
                booked.add(bd);
            }

            System.out.println("Transaction SUCCESS: All " + cart.size() + " bookings completed!");
            cart.clear(); // Leeg de cart na succes
            return booked;

        } catch (Exception e) {
            // Als er iets misgaat, rollback (verwijder alle geboekte items)
            System.out.println("Transaction ROLLBACK: Removing " + booked.size() + " bookings");
            rollback(booked);
            throw new Exception("Transaction failed and rolled back: " + e.getMessage());
        }
    }

    @Override
    public List<BookingDetail> getCart() throws RemoteException {
        return new ArrayList<>(cart);
    }

    @Override
    public void clearCart() throws RemoteException {
        cart.clear();
        System.out.println("Shopping cart cleared");
    }

    private void rollback(List<BookingDetail> booked) {
        // Verwijder alle bookings die al gemaakt waren
        for (BookingDetail bd : booked) {
            try {
                bookingManager.removeBooking(bd);
            } catch (Exception e) {
                System.err.println("Rollback error: " + e.getMessage());
            }
        }
    }
}