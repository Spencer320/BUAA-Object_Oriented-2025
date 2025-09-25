import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;

import java.util.ArrayList;
import java.util.HashMap;

public class BookShelf implements Location {
    private final HashMap<LibraryBookIsbn, ArrayList<LibraryBookId>> books;

    public BookShelf(HashMap<LibraryBookIsbn, ArrayList<LibraryBookId>> books) {
        this.books = books;
    }

    public boolean hasBook(LibraryBookIsbn isbn) {
        return books.containsKey(isbn) && (!books.get(isbn).isEmpty());
    }

    public LibraryBookId sendBook(LibraryBookIsbn isbn) {
        ArrayList<LibraryBookId> bookArrayList = books.get(isbn);
        return bookArrayList.remove(0);
    }

    public void receiveBook(LibraryBookId id) {
        LibraryBookIsbn isbn = id.getBookIsbn();
        ArrayList<LibraryBookId> bookArrayList = books.get(isbn);
        bookArrayList.add(id);
    }
}
