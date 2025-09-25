import com.oocourse.library1.LibraryBookId;

import java.util.HashSet;

public class BorrowReturnOffice implements Location {
    private final HashSet<LibraryBookId> books = new HashSet<>();

    public void sendBook(LibraryBookId id) {
        books.remove(id);
    }

    public void receiveBook(LibraryBookId id) {
        books.add(id);
    }
}
