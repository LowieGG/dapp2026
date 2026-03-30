package hotel;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.rmi.server.UnicastRemoteObject;

public class BookingManager implements IBookingManager {
	private Room[] rooms;
	private Map<Integer, Lock> roomLocks; // Lock per room

	public BookingManager() {
		this.rooms = initializeRooms();
		this.roomLocks = new ConcurrentHashMap<>();

		// Maak een lock voor elke room
		for (Room room : rooms) {
			roomLocks.put(room.getRoomNumber(), new ReentrantLock());
		}
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
		Lock lock = roomLocks.get(roomNumber);
		if (lock == null) {
			return false;
		}

		lock.lock();
		try {
			// Simuleer traag proces (om concurrency problemen te testen) 100ms
			Thread.sleep(0);

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
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RemoteException("Thread interrupted", e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void addBooking(BookingDetail bookingDetail) throws RemoteException, Exception {
		Integer roomNumber = bookingDetail.getRoomNumber();
		Lock lock = roomLocks.get(roomNumber);

		if (lock == null) {
			throw new Exception("Room " + roomNumber + " does not exist");
		}

		lock.lock();
		try {
			// Simuleer traag proces (om concurrency problemen te testen)
			Thread.sleep(100);

			if (!isRoomAvailableInternal(roomNumber, bookingDetail.getDate())) {
				throw new Exception("Room " + roomNumber +
						" is not available on " + bookingDetail.getDate());
			}

			Room room = findRoom(roomNumber);
			if (room != null) {
				room.getBookings().add(bookingDetail);
				System.out.println("Booking added: " + bookingDetail.getGuest() +
						" in room " + bookingDetail.getRoomNumber() +
						" on " + bookingDetail.getDate() +
						" [Thread: " + Thread.currentThread().getName() + "]");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RemoteException("Thread interrupted", e);
		} finally {
			lock.unlock();
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

	@Override
	public IBookingSession createSession() throws RemoteException {
		try {
			BookingSession session = new BookingSession(this);
			// Exporteer de sessie als remote object
			IBookingSession stub = (IBookingSession) UnicastRemoteObject.exportObject(session, 0);
			System.out.println("New booking session created");
			return stub;
		} catch (Exception e) {
			throw new RemoteException("Failed to create session", e);
		}
	}

	@Override
	public void removeBooking(BookingDetail bookingDetail) throws RemoteException {
		Integer roomNumber = bookingDetail.getRoomNumber();
		Lock lock = roomLocks.get(roomNumber);

		if (lock == null) {
			return;
		}

		lock.lock();
		try {
			Room room = findRoom(roomNumber);
			if (room != null) {
				room.getBookings().removeIf(bd ->
						bd.getRoomNumber().equals(bookingDetail.getRoomNumber()) &&
								bd.getDate().equals(bookingDetail.getDate()) &&
								bd.getGuest().equals(bookingDetail.getGuest())
				);
				System.out.println("Booking removed (rollback): " + bookingDetail.getGuest() +
						" in room " + bookingDetail.getRoomNumber());
			}
		} finally {
			lock.unlock();
		}
	}

	// Interne methode die al binnen een lock zit (geen dubbele lock)
	private boolean isRoomAvailableInternal(Integer roomNumber, LocalDate date) {
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