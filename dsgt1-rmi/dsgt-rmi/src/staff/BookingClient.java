package staff;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.util.Set;
import hotel.BookingDetail;
import hotel.IBookingManager;

public class BookingClient extends AbstractScriptedSimpleTest {
	private IBookingManager bm = null;

	public static void main(String[] args) throws Exception {
		BookingClient client = new BookingClient();
		client.run();
	}

	public BookingClient() {
		try {
			// Look up the registered remote instance
			Registry registry = LocateRegistry.getRegistry("localhost");
			bm = (IBookingManager) registry.lookup("BookingManager");
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	@Override
	public boolean isRoomAvailable(Integer roomNumber, LocalDate date) {
		try {
			return bm.isRoomAvailable(roomNumber, date);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void addBooking(BookingDetail bookingDetail) throws Exception {
		try {
			bm.addBooking(bookingDetail);
		} catch (Exception e) {
			System.out.println("Booking failed: " + e.getMessage());
		}
	}

	@Override
	public Set<Integer> getAvailableRooms(LocalDate date) {
		try {
			return bm.getAvailableRooms(date);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Set<Integer> getAllRooms() {
		try {
			return bm.getAllRooms();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}