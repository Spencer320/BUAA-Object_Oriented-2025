import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.LibraryBookState;

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
