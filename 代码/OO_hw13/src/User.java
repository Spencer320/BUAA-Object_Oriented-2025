import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;

import java.util.HashSet;

public class User {
    private final String id;
    private LibraryBookIsbn orderedBook = null;
    private final HashSet<LibraryBookId> books = new HashSet<>();

    public User(String id) {
        this.id = id;
    }

    public void sendBook(LibraryBookId id) {
        books.remove(id);
    }

    public void receiveBook(LibraryBookId id) {
        books.add(id);
    }

    public LibraryBookIsbn getOrderedBook() {
        return orderedBook;
    }

    public void setOrderedBook(LibraryBookIsbn orderedBook) {
        this.orderedBook = orderedBook;
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
}
