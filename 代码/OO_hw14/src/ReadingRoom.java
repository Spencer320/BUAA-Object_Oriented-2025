import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookState;

import java.util.HashSet;

public class ReadingRoom implements Location {
    private final HashSet<LibraryBookId> books = new HashSet<>();

    public ReadingRoom() {
    }

    public void sendBook(LibraryBookId id) {
        books.remove(id);
    }

    public void receiveBook(LibraryBookId id) {
        books.add(id);
    }

    @Override
    public LibraryBookState getAssociatedState() {
        return LibraryBookState.READING_ROOM;
    }
}
