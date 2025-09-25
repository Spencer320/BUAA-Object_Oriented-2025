import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;

public class User {
    private final String id;
    private LibraryBookIsbn orderedBook = null;
    private LibraryBookId readingBook = null;
    private final HashSet<LibraryBookId> books = new HashSet<>();
    private final HashMap<LibraryBookId, LocalDate> startTime = new HashMap<>();
    private int credit = 100;

    public User(String id) {
        this.id = id;
    }

    public void sendBook(LibraryBookId id) {
        books.remove(id);
        startTime.remove(id);
    }

    public void receiveBook(LocalDate date, LibraryBookId id) {
        books.add(id);
        startTime.put(id, date);
    }

    public LibraryBookIsbn getOrderedBook() {
        return orderedBook;
    }

    public void setOrderedBook(LibraryBookIsbn orderedBook) {
        this.orderedBook = orderedBook;
    }

    public LibraryBookId getReadingBook() {
        return readingBook;
    }

    public void setReadingBook(LibraryBookId readingBook) {
        this.readingBook = readingBook;
    }

    public boolean hasBookTypeB() {
        for (LibraryBookId id : books) {
            if (id.isTypeB()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBookTypeCAndIsbn(LibraryBookIsbn isbn) {
        for (LibraryBookId id : books) {
            if (id.isTypeC() && id.getBookIsbn().equals(isbn)) {
                return true;
            }
        }
        return false;
    }

    public int getCredit(LocalDate date) {
        return getRealCredit(date);
    }

    private int getRealCredit(LocalDate date) {
        int value = 0;
        for (LibraryBookId id : books) {
            if (isOverdue(id, date)) {
                value += getOverdueTime(id, date);
            }
        }
        return Math.max(0, credit - 5 * value);
    }

    public void modifyCredit(int value) {
        credit += value;
        int maxCredit = 180;
        credit = Math.min(maxCredit, credit);
        int minCredit = 0;
        credit = Math.max(minCredit, credit);
    }

    public boolean accessibleReadingBookTypeBAndC(LocalDate date) {
        return getRealCredit(date) > 0;
    }

    public boolean accessibleReadingBookTypeA(LocalDate date) {
        return getRealCredit(date) >= 40;
    }

    public boolean accessibleBorrowingBook(LocalDate date) {
        return getRealCredit(date) >= 60;
    }

    public boolean accessibleOrderingBook(LocalDate date) {
        return getRealCredit(date) >= 100;
    }

    public boolean isOverdue(LibraryBookId id, LocalDate date) {
        int dateLimitTypeB = 30;
        int dateLimitTypeC = 60;
        return (startTime.get(id).until(date, ChronoUnit.DAYS) >
            (id.isTypeB() ? dateLimitTypeB : dateLimitTypeC));
    }

    public int getOverdueTime(LibraryBookId id, LocalDate date) {
        int dateLimitTypeB = 30;
        int dateLimitTypeC = 60;
        return (int) (startTime.get(id).until(date, ChronoUnit.DAYS) -
            (id.isTypeB() ? dateLimitTypeB : dateLimitTypeC));
    }
}
