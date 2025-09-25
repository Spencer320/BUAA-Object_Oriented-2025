import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryBookState;

import java.util.ArrayList;
import java.util.HashMap;

public class HotBookshelf extends Bookshelf {
    public HotBookshelf(HashMap<LibraryBookIsbn, ArrayList<LibraryBookId>> books) {
        super(books);
    }

    @Override
    public LibraryBookState getAssociatedState() {
        return LibraryBookState.HOT_BOOKSHELF;
    }
}
