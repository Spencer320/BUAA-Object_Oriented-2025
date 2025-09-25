import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;
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
    private final BookShelf bookShelf;
    private final BorrowReturnOffice borrowReturnOffice = new BorrowReturnOffice();
    private final AppointmentOffice appointmentOffice = new AppointmentOffice();
    private final Map<String, User> users = new HashMap<>();

    public Manager(Map<LibraryBookId, Book> books) {
        this.books = books;
        this.bookShelf = createBookShelf();
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

    private Book getBook(LibraryBookId id) {
        return books.get(id);
    }

    private User getUser(String id) {
        if (!users.containsKey(id)) {
            users.put(id, new User(id));
        }
        return users.get(id);
    }

    public void rearrangeBooks(LocalDate date) {
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();
        for (LibraryBookId bookId : books.keySet()) {
            Book book = books.get(bookId);
            if (book.getCurrentState() == APPOINTMENT_OFFICE) {
                if (appointmentOffice.isUseUpTime(bookId, date)) {
                    infos.add(new LibraryMoveInfo(bookId, APPOINTMENT_OFFICE, BOOKSHELF));
                    String userId = appointmentOffice.sendBook(bookId);
                    book.setCurrentState(date, BOOKSHELF);
                    bookShelf.receiveBook(bookId);
                    getUser(userId).setOrderedBook(null);
                }
            } else if (book.getCurrentState() == BORROW_RETURN_OFFICE) {
                infos.add(new LibraryMoveInfo(bookId, BORROW_RETURN_OFFICE, BOOKSHELF));
                borrowReturnOffice.sendBook(bookId);
                book.setCurrentState(date, BOOKSHELF);
                bookShelf.receiveBook(bookId);
            }
        }
        for (String userId : users.keySet()) {
            LibraryBookIsbn orderedBook = getUser(userId).getOrderedBook();
            boolean assignable = (orderedBook != null) &&
                bookShelf.hasBook(orderedBook);
            if (assignable) {
                LibraryBookId copyId = bookShelf.sendBook(orderedBook);
                infos.add(new LibraryMoveInfo(copyId, BOOKSHELF, APPOINTMENT_OFFICE, userId));
                getBook(copyId).setCurrentState(date, APPOINTMENT_OFFICE);
                appointmentOffice.receiveBook(date, copyId, userId);
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
        User user = getUser(userId);
        boolean available = (!((isbn.isTypeA()) ||
            (isbn.isTypeB() && user.hasBookTypeB()) ||
            (isbn.isTypeC() && user.hasBookTypeCAndIsbn(isbn)))) &&
            bookShelf.hasBook(isbn);
        if (available) {
            LibraryBookId copyId = bookShelf.sendBook(isbn);
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
        User user = getUser(userId);
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
        User user = getUser(userId);
        user.sendBook(bookId);
        getBook(bookId).setCurrentState(today, BORROW_RETURN_OFFICE);
        borrowReturnOffice.receiveBook(bookId);
        PRINTER.accept(req);
    }

    public void pickBook(LocalDate today, LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookIsbn isbn = req.getBookIsbn();
        User user = getUser(userId);
        boolean available = (!((isbn.isTypeB() && user.hasBookTypeB()) ||
            (isbn.isTypeC() && user.hasBookTypeCAndIsbn(isbn)))) &&
            (appointmentOffice.getBookForUser(userId) != null);
        if (available) {
            LibraryBookId copyId = appointmentOffice.getBookForUser(userId);
            appointmentOffice.sendBook(copyId);
            getBook(copyId).setCurrentState(today, USER);
            user.receiveBook(copyId);
            user.setOrderedBook(null);
            PRINTER.accept(req, copyId);
        } else {
            PRINTER.reject(req);
        }
    }

}
