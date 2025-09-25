import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.LibraryBookState;
import com.oocourse.library3.LibraryCloseCmd;
import com.oocourse.library3.LibraryMoveInfo;
import com.oocourse.library3.LibraryOpenCmd;
import com.oocourse.library3.LibraryQcsCmd;
import com.oocourse.library3.LibraryReqCmd;
import com.oocourse.library3.LibraryTrace;
import com.oocourse.library3.annotation.Trigger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.oocourse.library3.LibraryBookState.APPOINTMENT_OFFICE;
import static com.oocourse.library3.LibraryBookState.BOOKSHELF;
import static com.oocourse.library3.LibraryBookState.BORROW_RETURN_OFFICE;
import static com.oocourse.library3.LibraryBookState.HOT_BOOKSHELF;
import static com.oocourse.library3.LibraryBookState.READING_ROOM;
import static com.oocourse.library3.LibraryBookState.USER;
import static com.oocourse.library3.LibraryIO.PRINTER;

public class Manager {
    private final Map<LibraryBookId, Book> books;
    private final Set<LibraryBookIsbn> isbns;
    private final Map<LibraryBookState, Location> locations;
    private final Set<LibraryBookIsbn> hotIsbn = new HashSet<>();

    public Manager(Map<LibraryBookId, Book> books, Set<LibraryBookIsbn> isbns) {
        this.books = books;
        this.isbns = isbns;
        this.locations = createLocations();
    }

    private HashMap<LibraryBookState, Location> createLocations() {
        HashMap<LibraryBookState, Location> locationHashMap = new HashMap<>();
        HashMap<LibraryBookIsbn, ArrayList<LibraryBookId>> bookMap = new HashMap<>();
        HashMap<LibraryBookIsbn, ArrayList<LibraryBookId>> bookEmptyMap = new HashMap<>();
        for (LibraryBookId bookId : books.keySet()) {
            LibraryBookIsbn bookIsbn = bookId.getBookIsbn();
            bookMap.computeIfAbsent(bookIsbn, k -> new ArrayList<>()).add(bookId);
            bookEmptyMap.computeIfAbsent(bookIsbn, k -> new ArrayList<>());
        }
        locationHashMap.put(BOOKSHELF, new Bookshelf(bookMap));
        locationHashMap.put(HOT_BOOKSHELF, new HotBookshelf(bookEmptyMap));
        locationHashMap.put(BORROW_RETURN_OFFICE, new BorrowReturnOffice());
        locationHashMap.put(APPOINTMENT_OFFICE, new AppointmentOffice());
        locationHashMap.put(USER, new Users());
        locationHashMap.put(READING_ROOM, new ReadingRoom());
        return locationHashMap;
    }

    @SuppressWarnings("unchecked")
    public <T extends Location> T getLocationAs(LibraryBookState state) {
        Location loc = locations.get(state);
        return (T) loc;
    }

    private Book getBook(LibraryBookId id) {
        return books.get(id);
    }

    @Trigger(from = "BORROW_RETURN_OFFICE", to = {"BOOKSHELF", "HOT_BOOKSHELF"})
    @Trigger(from = "APPOINTMENT_OFFICE", to = {"BOOKSHELF", "HOT_BOOKSHELF"})
    @Trigger(from = "READING_ROOM", to = {"BOOKSHELF", "HOT_BOOKSHELF"})
    @Trigger(from = "BOOKSHELF", to = {"APPOINTMENT_OFFICE", "HOT_BOOKSHELF"})
    @Trigger(from = "HOT_BOOKSHELF", to = {"APPOINTMENT_OFFICE", "BOOKSHELF"})
    public void rearrangeBooks(LibraryOpenCmd cmd) {
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();
        /* Step1: clear bro, rr and ao(out of date) books; move to bs\hbs */
        processRearrangeStep1(cmd.getDate(), infos);
        /* Step2: move ordered books from bs\hbs to ao; clear reading books */
        processRearrangeStep2(cmd.getDate(), infos);
        /* Step3: exchange between bs\hbs, clear hot isbn */
        processRearrangeStep3(cmd.getDate(), infos);
        PRINTER.move(cmd.getDate(), infos);
    }

    private void processRearrangeStep1(LocalDate date, ArrayList<LibraryMoveInfo> infos) {
        AppointmentOffice ao = getLocationAs(APPOINTMENT_OFFICE);
        Bookshelf bs = getLocationAs(BOOKSHELF);
        HotBookshelf hbs = getLocationAs(HOT_BOOKSHELF);
        BorrowReturnOffice bro = getLocationAs(BORROW_RETURN_OFFICE);
        Users users = getLocationAs(USER);
        ReadingRoom rr = getLocationAs(READING_ROOM);
        for (LibraryBookId bookId : books.keySet()) {
            Book book = books.get(bookId);
            boolean isHot = hotIsbn.contains(bookId.getBookIsbn());
            if (book.getCurrentState() == APPOINTMENT_OFFICE) {
                if (ao.isUseUpTime(bookId, date)) {
                    infos.add(new LibraryMoveInfo(bookId, APPOINTMENT_OFFICE,
                        (isHot ? HOT_BOOKSHELF : BOOKSHELF)));
                    String userId = ao.sendBook(bookId);
                    book.setCurrentState(date, (isHot ? HOT_BOOKSHELF : BOOKSHELF));
                    (isHot ? hbs : bs).receiveBook(bookId);
                    users.getUser(userId).setOrderedBook(null);
                    users.getUser(userId).modifyCredit(-15);
                }
            } else if (book.getCurrentState() == BORROW_RETURN_OFFICE) {
                infos.add(new LibraryMoveInfo(bookId, BORROW_RETURN_OFFICE,
                    (isHot ? HOT_BOOKSHELF : BOOKSHELF)));
                bro.sendBook(bookId);
                book.setCurrentState(date, (isHot ? HOT_BOOKSHELF : BOOKSHELF));
                (isHot ? hbs : bs).receiveBook(bookId);
            } else if (book.getCurrentState() == READING_ROOM) {
                infos.add(new LibraryMoveInfo(bookId, READING_ROOM,
                    (isHot ? HOT_BOOKSHELF : BOOKSHELF)));
                rr.sendBook(bookId);
                book.setCurrentState(date, (isHot ? HOT_BOOKSHELF : BOOKSHELF));
                (isHot ? hbs : bs).receiveBook(bookId);
            }
        }
    }

    private void processRearrangeStep2(LocalDate date, ArrayList<LibraryMoveInfo> infos) {
        AppointmentOffice ao = getLocationAs(APPOINTMENT_OFFICE);
        Bookshelf bs = getLocationAs(BOOKSHELF);
        HotBookshelf hbs = getLocationAs(HOT_BOOKSHELF);
        Users users = getLocationAs(USER);
        for (String userId : users.getUserIds()) {
            User user = users.getUser(userId);
            LibraryBookIsbn orderedBook = user.getOrderedBook();
            boolean assignable = (orderedBook != null) &&
                (bs.hasBook(orderedBook) || hbs.hasBook(orderedBook));
            if (assignable) {
                boolean isBookshelf = bs.hasBook(orderedBook);
                LibraryBookId copyId = (isBookshelf ? bs : hbs).sendBook(orderedBook);
                infos.add(new LibraryMoveInfo(copyId,
                    isBookshelf ? BOOKSHELF : HOT_BOOKSHELF, APPOINTMENT_OFFICE, userId));
                getBook(copyId).setCurrentState(date, APPOINTMENT_OFFICE);
                ao.receiveBook(date, copyId, userId);
            }
            if (user.getReadingBook() != null) {
                user.setReadingBook(null);
                user.modifyCredit(-10);
            }
        }
    }

    private void processRearrangeStep3(LocalDate date, ArrayList<LibraryMoveInfo> infos) {
        Bookshelf bs = getLocationAs(BOOKSHELF);
        HotBookshelf hbs = getLocationAs(HOT_BOOKSHELF);
        for (LibraryBookIsbn isbn : isbns) {
            boolean isHot = hotIsbn.contains(isbn);
            if (isHot && bs.hasBook(isbn)) {
                int left = bs.leftBooks(isbn);
                for (int i = 0; i < left; i++) {
                    LibraryBookId copyId = bs.sendBook(isbn);
                    infos.add(new LibraryMoveInfo(copyId, BOOKSHELF, HOT_BOOKSHELF));
                    getBook(copyId).setCurrentState(date, HOT_BOOKSHELF);
                    hbs.receiveBook(copyId);
                }
            } else if (!isHot && hbs.hasBook(isbn)) {
                int left = hbs.leftBooks(isbn);
                for (int i = 0; i < left; i++) {
                    LibraryBookId copyId = hbs.sendBook(isbn);
                    infos.add(new LibraryMoveInfo(copyId, HOT_BOOKSHELF, BOOKSHELF));
                    getBook(copyId).setCurrentState(date, BOOKSHELF);
                    bs.receiveBook(copyId);
                }
            }
        }
        hotIsbn.clear();
    }

    public void nap(LibraryCloseCmd cmd) {
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();
        PRINTER.move(cmd.getDate(), infos);
    }

    public void queryBookHistoryLine(LibraryReqCmd req) {
        LibraryBookId id = req.getBookId();
        ArrayList<LibraryTrace> historyLine = getBook(id).getHistoryLine();
        PRINTER.info(req.getDate(), id, historyLine);
    }

    @Trigger(from = "BOOKSHELF", to = "USER")
    @Trigger(from = "HOT_BOOKSHELF", to = "USER")
    public void borrowBook(LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookIsbn isbn = req.getBookIsbn();
        Bookshelf bs = getLocationAs(BOOKSHELF);
        HotBookshelf hbs = getLocationAs(HOT_BOOKSHELF);
        User user = this.<Users>getLocationAs(USER).getUser(userId);
        boolean available = (!(isbn.isTypeA() ||
            (isbn.isTypeB() && user.hasBookTypeB()) ||
            (isbn.isTypeC() && user.hasBookTypeCAndIsbn(isbn))));
        available &= (bs.hasBook(isbn) || hbs.hasBook(isbn)) &&
            user.accessibleBorrowingBook(req.getDate());
        if (available) {
            LibraryBookId copyId = (bs.hasBook(isbn)) ?
                bs.sendBook(isbn) : hbs.sendBook(isbn);
            getBook(copyId).setCurrentState(req.getDate(), USER);
            user.receiveBook(req.getDate(), copyId);
            hotIsbn.add(isbn);
            PRINTER.accept(req, copyId);
        } else {
            PRINTER.reject(req);
        }
    }

    public void orderBook(LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookIsbn isbn = req.getBookIsbn();
        User user = this.<Users>getLocationAs(USER).getUser(userId);
        boolean available = (!((isbn.isTypeA()) ||
            (isbn.isTypeB() && user.hasBookTypeB()) ||
            (isbn.isTypeC() && user.hasBookTypeCAndIsbn(isbn)) ||
            (user.getOrderedBook() != null))) &&
            user.accessibleOrderingBook(req.getDate());
        if (available) {
            user.setOrderedBook(isbn);
            PRINTER.accept(req);
        } else {
            PRINTER.reject(req);
        }
    }

    public void orderNewBook() {
        // place holder
    }

    @Trigger(from = "USER", to = "BORROW_RETURN_OFFICE")
    public void returnBook(LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookId bookId = req.getBookId();
        User user = this.<Users>getLocationAs(USER).getUser(userId);
        String additionalInfo;
        if (user.isOverdue(bookId, req.getDate())) {
            additionalInfo = "overdue";
            user.modifyCredit(-5 * user.getOverdueTime(bookId, req.getDate()));
        } else {
            additionalInfo = "not overdue";
            user.modifyCredit(+10);
        }
        user.sendBook(bookId);
        getBook(bookId).setCurrentState(req.getDate(), BORROW_RETURN_OFFICE);
        this.<BorrowReturnOffice>getLocationAs(BORROW_RETURN_OFFICE).receiveBook(bookId);
        PRINTER.accept(req, additionalInfo);
    }

    @Trigger(from = "APPOINTMENT_OFFICE", to = "USER")
    public void pickBook(LibraryReqCmd req) {
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
            getBook(copyId).setCurrentState(req.getDate(), USER);
            user.receiveBook(req.getDate(), copyId);
            user.setOrderedBook(null);
            PRINTER.accept(req, copyId);
        } else {
            PRINTER.reject(req);
        }
    }

    public void getOrderedBook() {
        // place holder
    }

    @Trigger(from = "BOOKSHELF", to = "READING_ROOM")
    @Trigger(from = "HOT_BOOKSHELF", to = "READING_ROOM")
    public void readBook(LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookIsbn isbn = req.getBookIsbn();
        Bookshelf bs = getLocationAs(BOOKSHELF);
        HotBookshelf hbs = getLocationAs(HOT_BOOKSHELF);
        User user = this.<Users>getLocationAs(USER).getUser(userId);
        boolean available = (user.getReadingBook() == null) &&
            (bs.hasBook(isbn) || hbs.hasBook(isbn));
        available &= ((isbn.isTypeA() && user.accessibleReadingBookTypeA(req.getDate())) ||
            !isbn.isTypeA() && user.accessibleReadingBookTypeBAndC(req.getDate()));
        if (available) {
            LibraryBookId copyId = (bs.hasBook(isbn)) ?
                bs.sendBook(isbn) : hbs.sendBook(isbn);
            getBook(copyId).setCurrentState(req.getDate(), READING_ROOM);
            user.setReadingBook(copyId);
            this.<ReadingRoom>getLocationAs(READING_ROOM).receiveBook(copyId);
            PRINTER.accept(req, copyId);
            hotIsbn.add(isbn);
        } else {
            PRINTER.reject(req);
        }
    }

    @Trigger(from = "READING_ROOM", to = "BORROW_RETURN_OFFICE")
    public void restoreBook(LibraryReqCmd req) {
        String userId = req.getStudentId();
        User user = this.<Users>getLocationAs(USER).getUser(userId);
        LibraryBookId bookId = req.getBookId();
        this.<ReadingRoom>getLocationAs(READING_ROOM).sendBook(bookId);
        getBook(bookId).setCurrentState(req.getDate(), BORROW_RETURN_OFFICE);
        user.setReadingBook(null);
        this.<BorrowReturnOffice>getLocationAs(BORROW_RETURN_OFFICE).receiveBook(bookId);
        user.modifyCredit(+10);
        PRINTER.accept(req);
    }

    public void queryCredit(LibraryQcsCmd cmd) {
        String userId = cmd.getStudentId();
        PRINTER.info(cmd, this.<Users>getLocationAs(USER).getUser(userId).getCredit(cmd.getDate()));
    }
}
