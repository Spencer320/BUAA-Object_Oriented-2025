import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookState;

import java.util.HashSet;

public class BorrowReturnOffice implements Location {
    private final HashSet<LibraryBookId> books = new HashSet<>();

    public BorrowReturnOffice() {
    }

    public void sendBook(LibraryBookId id) {
        books.remove(id);
    }

    public void receiveBook(LibraryBookId id) {
        books.add(id);
    }

    @Override
    public LibraryBookState getAssociatedState() {
        return LibraryBookState.BORROW_RETURN_OFFICE;
    }
}
