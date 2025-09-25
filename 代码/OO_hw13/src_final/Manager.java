import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryBookState;
import com.oocourse.library1.LibraryMoveInfo;
import com.oocourse.library1.LibraryReqCmd;
import com.oocourse.library1.LibraryTrace;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.oocourse.library1.LibraryBookState.APPOINTMENT_OFFICE;
import static com.oocourse.library1.LibraryBookState.BOOKSHELF;
import static com.oocourse.library1.LibraryBookState.BORROW_RETURN_OFFICE;
import static com.oocourse.library1.LibraryBookState.USER;
import static com.oocourse.library1.LibraryIO.PRINTER;

public class Manager {
    private final Map<LibraryBookId, Book> books;
    private final Map<LibraryBookState, Location> locations;

    public Manager(Map<LibraryBookId, Book> books) {
        this.books = books;
        this.locations = createLocations();
    }

    private HashMap<LibraryBookState, Location> createLocations() {
        HashMap<LibraryBookState, Location> locationHashMap = new HashMap<>();
        locationHashMap.put(BOOKSHELF, createBookShelf());
        locationHashMap.put(BORROW_RETURN_OFFICE, new BorrowReturnOffice());
        locationHashMap.put(APPOINTMENT_OFFICE, new AppointmentOffice());
        locationHashMap.put(USER, new Users());
        return locationHashMap;
    }

    private BookShelf createBookShelf() {
        HashMap<LibraryBookIsbn, ArrayList<LibraryBookId>> bookMap = new HashMap<>();
        for (LibraryBookId bookId : books.keySet()) {
            LibraryBookIsbn bookIsbn = bookId.getBookIsbn();
            if (!bookMap.containsKey(bookIsbn)) {
                bookMap.put(bookIsbn, new ArrayList<>());
            }
            bookMap.get(bookIsbn).add(bookId);
        }
        return new BookShelf(bookMap);
    }

    @SuppressWarnings("unchecked")
    public <T extends Location> T getLocationAs(LibraryBookState state) {
        Location loc = locations.get(state);
        return (T) loc;
    }

    private Book getBook(LibraryBookId id) {
        return books.get(id);
    }

    public void rearrangeBooks(LocalDate date) {
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();
        AppointmentOffice ao = getLocationAs(APPOINTMENT_OFFICE);
        BookShelf bs = getLocationAs(BOOKSHELF);
        BorrowReturnOffice bro = getLocationAs(BORROW_RETURN_OFFICE);
        Users users = getLocationAs(USER);
        for (LibraryBookId bookId : books.keySet()) {
            Book book = books.get(bookId);
            if (book.getCurrentState() == APPOINTMENT_OFFICE) {
                if (ao.isUseUpTime(bookId, date)) {
                    infos.add(new LibraryMoveInfo(bookId, APPOINTMENT_OFFICE, BOOKSHELF));
                    String userId = ao.sendBook(bookId);
                    book.setCurrentState(date, BOOKSHELF);
                    bs.receiveBook(bookId);
                    users.getUser(userId).setOrderedBook(null);
                }
            } else if (book.getCurrentState() == BORROW_RETURN_OFFICE) {
                infos.add(new LibraryMoveInfo(bookId, BORROW_RETURN_OFFICE, BOOKSHELF));
                bro.sendBook(bookId);
                book.setCurrentState(date, BOOKSHELF);
                bs.receiveBook(bookId);
            }
        }
        for (String userId : users.getUserIds()) {
            LibraryBookIsbn orderedBook = users.getUser(userId).getOrderedBook();
            boolean assignable = (orderedBook != null) &&
                bs.hasBook(orderedBook);
            if (assignable) {
                LibraryBookId copyId = bs.sendBook(orderedBook);
                infos.add(new LibraryMoveInfo(copyId, BOOKSHELF, APPOINTMENT_OFFICE, userId));
                getBook(copyId).setCurrentState(date, APPOINTMENT_OFFICE);
                ao.receiveBook(date, copyId, userId);
            }
        }
        PRINTER.move(date, infos);
    }

    public void queryBookHistoryLine(LocalDate date, LibraryReqCmd req) {
        LibraryBookId id = req.getBookId();
        ArrayList<LibraryTrace> historyLine = getBook(id).getHistoryLine();
        PRINTER.info(date, id, historyLine);
    }

    public void borrowBook(LocalDate date, LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookIsbn isbn = req.getBookIsbn();
        BookShelf bs = getLocationAs(BOOKSHELF);
        User user = this.<Users>getLocationAs(USER).getUser(userId);
        boolean available = (!((isbn.isTypeA()) ||
            (isbn.isTypeB() && user.hasBookTypeB()) ||
            (isbn.isTypeC() && user.hasBookTypeCAndIsbn(isbn)))) &&
            bs.hasBook(isbn);
        if (available) {
            LibraryBookId copyId = bs.sendBook(isbn);
            getBook(copyId).setCurrentState(date, USER);
            user.receiveBook(copyId);
            PRINTER.accept(req, copyId);
        } else {
            PRINTER.reject(req);
        }
    }

    public void orderBook(LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookIsbn isbn = req.getBookIsbn();
        User user = this.<Users>getLocationAs(USER).getUser(userId);
        boolean available = !((isbn.isTypeA()) ||
            (isbn.isTypeB() && user.hasBookTypeB()) ||
            (isbn.isTypeC() && user.hasBookTypeCAndIsbn(isbn)) ||
            (user.getOrderedBook() != null));
        if (available) {
            user.setOrderedBook(isbn);
            PRINTER.accept(req);
        } else {
            PRINTER.reject(req);
        }
    }

    public void returnBook(LocalDate today, LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookId bookId = req.getBookId();
        this.<Users>getLocationAs(USER).getUser(userId).sendBook(bookId);
        getBook(bookId).setCurrentState(today, BORROW_RETURN_OFFICE);
        this.<BorrowReturnOffice>getLocationAs(BORROW_RETURN_OFFICE).receiveBook(bookId);
        PRINTER.accept(req);
    }

    public void pickBook(LocalDate today, LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookIsbn isbn = req.getBookIsbn();
        AppointmentOffice ao = getLocationAs(APPOINTMENT_OFFICE);
        User user = this.<Users>getLocationAs(USER).getUser(userId);
        boolean available = (!((isbn.isTypeB() && user.hasBookTypeB()) ||
            (isbn.isTypeC() && user.hasBookTypeCAndIsbn(isbn)))) &&
            (ao.getBookForUser(userId) != null);
        if (available) {
            LibraryBookId copyId = ao.getBookForUser(userId);
            ao.sendBook(copyId);
            getBook(copyId).setCurrentState(today, USER);
            user.receiveBook(copyId);
            user.setOrderedBook(null);
            PRINTER.accept(req, copyId);
        } else {
            PRINTER.reject(req);
        }
    }
}
