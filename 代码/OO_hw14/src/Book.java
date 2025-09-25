import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookState;
import com.oocourse.library2.LibraryTrace;

import java.time.LocalDate;
import java.util.ArrayList;

import static com.oocourse.library2.LibraryBookState.BOOKSHELF;

public class Book {
    private LibraryBookId id;
    private LibraryBookState currentState = BOOKSHELF;
    private final ArrayList<LibraryTrace> historyLine = new ArrayList<>();

    public Book(LibraryBookId id) {
        this.id = id;
    }

    public LibraryBookId getId() {
        return id;
    }

    public LibraryBookState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(LocalDate date, LibraryBookState state) {
        historyLine.add(new LibraryTrace(date, currentState, state));
        this.currentState = state;
    }

    public ArrayList<LibraryTrace> getHistoryLine() {
        return historyLine;
    }
}
