import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookState;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;

public class AppointmentOffice implements Location {
    private final HashSet<LibraryBookId> books = new HashSet<>();
    private final HashMap<LibraryBookId, LocalDate> startTime = new HashMap<>();
    private final HashMap<LibraryBookId, String> orderedUser = new HashMap<>();

    public AppointmentOffice() {
    }

    public boolean isUseUpTime(LibraryBookId id, LocalDate date) {
        return (startTime.get(id).until(date, ChronoUnit.DAYS) >= 5);
    }

    public LibraryBookId getBookForUser(String userId) {
        for (LibraryBookId bookId : books) {
            if (orderedUser.get(bookId).equals(userId)) {
                return bookId;
            }
        }
        return null;
    }

    public String sendBook(LibraryBookId id) {
        books.remove(id);
        startTime.remove(id);
        return orderedUser.remove(id);
    }

    public void receiveBook(LocalDate date, LibraryBookId id, String userId) {
        books.add(id);
        startTime.put(id, date);
        orderedUser.put(id, userId);
    }

    @Override
    public LibraryBookState getAssociatedState() {
        return LibraryBookState.APPOINTMENT_OFFICE;
    }
}
