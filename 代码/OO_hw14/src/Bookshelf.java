import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryBookState;

import java.util.ArrayList;
import java.util.HashMap;

public class Bookshelf implements Location {
    private final HashMap<LibraryBookIsbn, ArrayList<LibraryBookId>> books;

    public Bookshelf(HashMap<LibraryBookIsbn, ArrayList<LibraryBookId>> books) {
        this.books = books;
    }

    public boolean hasBook(LibraryBookIsbn isbn) {
        return books.containsKey(isbn) && (!books.get(isbn).isEmpty());
    }

    public Integer leftBooks(LibraryBookIsbn isbn) {
        return books.get(isbn).size();
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

    @Override
    public LibraryBookState getAssociatedState() {
        return LibraryBookState.BOOKSHELF;
    }
}
