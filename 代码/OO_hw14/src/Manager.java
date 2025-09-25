import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryBookState;
import com.oocourse.library2.LibraryMoveInfo;
import com.oocourse.library2.LibraryReqCmd;
import com.oocourse.library2.LibraryTrace;
import com.oocourse.library2.annotation.Trigger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.oocourse.library2.LibraryBookState.APPOINTMENT_OFFICE;
import static com.oocourse.library2.LibraryBookState.BOOKSHELF;
import static com.oocourse.library2.LibraryBookState.BORROW_RETURN_OFFICE;
import static com.oocourse.library2.LibraryBookState.HOT_BOOKSHELF;
import static com.oocourse.library2.LibraryBookState.READING_ROOM;
import static com.oocourse.library2.LibraryBookState.USER;
import static com.oocourse.library2.LibraryIO.PRINTER;

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
    public void rearrangeBooks(LocalDate date) {
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();
        /* Step1: clear bro, rr and ao(out of date) books; move to bs\hbs */
        processRearrangeStep1(date, infos);
        /* Step2: move ordered books from bs\hbs to ao; clear reading books */
        processRearrangeStep2(date, infos);
        /* Step3: exchange between bs\hbs, clear hot isbn */
        processRearrangeStep3(date, infos);
        PRINTER.move(date, infos);
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
            user.setReadingBook(null);
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

    public void queryBookHistoryLine(LocalDate date, LibraryReqCmd req) {
        LibraryBookId id = req.getBookId();
        ArrayList<LibraryTrace> historyLine = getBook(id).getHistoryLine();
        PRINTER.info(date, id, historyLine);
    }

    @Trigger(from = "BOOKSHELF", to = "USER")
    @Trigger(from = "HOT_BOOKSHELF", to = "USER")
    public void borrowBook(LocalDate date, LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookIsbn isbn = req.getBookIsbn();
        Bookshelf bs = getLocationAs(BOOKSHELF);
        HotBookshelf hbs = getLocationAs(HOT_BOOKSHELF);
        User user = this.<Users>getLocationAs(USER).getUser(userId);
        boolean available = (!((isbn.isTypeA()) ||
            (isbn.isTypeB() && user.hasBookTypeB()) ||
            (isbn.isTypeC() && user.hasBookTypeCAndIsbn(isbn)))) &&
            (bs.hasBook(isbn) || hbs.hasBook(isbn));
        if (available) {
            LibraryBookId copyId = (bs.hasBook(isbn)) ?
                bs.sendBook(isbn) : hbs.sendBook(isbn);
            getBook(copyId).setCurrentState(date, USER);
            user.receiveBook(copyId);
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

    @Trigger(from = "USER", to = "BORROW_RETURN_OFFICE")
    public void returnBook(LocalDate today, LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookId bookId = req.getBookId();
        this.<Users>getLocationAs(USER).getUser(userId).sendBook(bookId);
        getBook(bookId).setCurrentState(today, BORROW_RETURN_OFFICE);
        this.<BorrowReturnOffice>getLocationAs(BORROW_RETURN_OFFICE).receiveBook(bookId);
        PRINTER.accept(req);
    }

    @Trigger(from = "APPOINTMENT_OFFICE", to = "USER")
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

    @Trigger(from = "BOOKSHELF", to = "READING_ROOM")
    @Trigger(from = "HOT_BOOKSHELF", to = "READING_ROOM")
    public void readBook(LocalDate today, LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookIsbn isbn = req.getBookIsbn();
        Bookshelf bs = getLocationAs(BOOKSHELF);
        HotBookshelf hbs = getLocationAs(HOT_BOOKSHELF);
        User user = this.<Users>getLocationAs(USER).getUser(userId);
        boolean available = (user.getReadingBook() == null) &&
            (bs.hasBook(isbn) || hbs.hasBook(isbn));
        if (available) {
            LibraryBookId copyId = (bs.hasBook(isbn)) ?
                bs.sendBook(isbn) : hbs.sendBook(isbn);
            getBook(copyId).setCurrentState(today, READING_ROOM);
            user.setReadingBook(copyId);
            this.<ReadingRoom>getLocationAs(READING_ROOM).receiveBook(copyId);
            PRINTER.accept(req, copyId);
            hotIsbn.add(isbn);
        } else {
            PRINTER.reject(req);
        }
    }

    @Trigger(from = "READING_ROOM", to = "BORROW_RETURN_OFFICE")
    public void restoreBook(LocalDate today, LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookId bookId = req.getBookId();
        this.<ReadingRoom>getLocationAs(READING_ROOM).sendBook(bookId);
        getBook(bookId).setCurrentState(today, BORROW_RETURN_OFFICE);
        this.<Users>getLocationAs(USER).getUser(userId).setReadingBook(null);
        this.<BorrowReturnOffice>getLocationAs(BORROW_RETURN_OFFICE).receiveBook(bookId);
        PRINTER.accept(req);
    }
}
