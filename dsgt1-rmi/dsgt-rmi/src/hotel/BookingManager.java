package hotel;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BookingManager implements IBookingManager {
	private Room[] rooms;

	public BookingManager() {
		this.rooms = initializeRooms();
	}

	@Override
	public Set<Integer> getAllRooms() throws RemoteException {
		Set<Integer> allRooms = new HashSet<Integer>();
		Iterable<Room> roomIterator = Arrays.asList(rooms);
		for (Room room : roomIterator) {
			allRooms.add(room.getRoomNumber());
		}
		return allRooms;
	}

	@Override
	public boolean isRoomAvailable(Integer roomNumber, LocalDate date) throws RemoteException {
		Room room = findRoom(roomNumber);
		if (room == null) {
			return false;
		}

		for (BookingDetail booking : room.getBookings()) {
			if (booking.getDate().equals(date)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void addBooking(BookingDetail bookingDetail) throws RemoteException, Exception {
		if (!isRoomAvailable(bookingDetail.getRoomNumber(), bookingDetail.getDate())) {
			throw new Exception("Room " + bookingDetail.getRoomNumber() +
					" is not available on " + bookingDetail.getDate());
		}

		Room room = findRoom(bookingDetail.getRoomNumber());
		if (room != null) {
			room.getBookings().add(bookingDetail);
			System.out.println("Booking added: " + bookingDetail.getGuest() +
					" in room " + bookingDetail.getRoomNumber() +
					" on " + bookingDetail.getDate());
		}
	}

	@Override
	public Set<Integer> getAvailableRooms(LocalDate date) throws RemoteException {
		Set<Integer> availableRooms = new HashSet<Integer>();
		for (Room room : rooms) {
			if (isRoomAvailable(room.getRoomNumber(), date)) {
				availableRooms.add(room.getRoomNumber());
			}
		}
		return availableRooms;
	}

	private Room findRoom(Integer roomNumber) {
		for (Room room : rooms) {
			if (room.getRoomNumber().equals(roomNumber)) {
				return room;
			}
		}
		return null;
	}

	private static Room[] initializeRooms() {
		Room[] rooms = new Room[4];
		rooms[0] = new Room(101);
		rooms[1] = new Room(102);
		rooms[2] = new Room(201);
		rooms[3] = new Room(203);
		return rooms;
	}
}