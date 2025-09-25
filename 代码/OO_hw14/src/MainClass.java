import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryCloseCmd;
import com.oocourse.library2.LibraryCommand;
import com.oocourse.library2.LibraryOpenCmd;
import com.oocourse.library2.LibraryReqCmd;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static com.oocourse.library2.LibraryIO.SCANNER;

public class MainClass {
    public static void main(String[] args) {
        Map<LibraryBookIsbn, Integer> bookList = SCANNER.getInventory();    // 获取图书馆内所有书籍ISBN号及相应副本数
        Map<LibraryBookId, Book> books = new HashMap<>();                   // 创建书籍副本号
        for (LibraryBookIsbn isbn : bookList.keySet()) {
            LibraryBookIsbn.Type type = isbn.getType();
            String uid = isbn.getUid();
            String copyId;
            int copyNum = bookList.get(isbn);
            for (int i = 1; i <= copyNum; i++) {
                copyId = String.format("%02d", i);
                LibraryBookId id = new LibraryBookId(type, uid, copyId);
                books.put(id, new Book(id));
            }
        }
        Manager manager = new Manager(books,bookList.keySet());              // 创建图书管理系统

        while (true) {
            LibraryCommand command = SCANNER.nextCommand();
            if (command == null) {
                break;
            }
            LocalDate today = command.getDate();
            if (command instanceof LibraryOpenCmd) {
                manager.rearrangeBooks(today);
            } else if (command instanceof LibraryCloseCmd) {
                System.out.println("0");
                continue;
            } else {
                LibraryReqCmd req = (LibraryReqCmd) command;
                LibraryReqCmd.Type type = req.getType();
                switch (type) {
                    case QUERIED:
                        manager.queryBookHistoryLine(today, req);
                        break;
                    case BORROWED:
                        manager.borrowBook(today, req);
                        break;
                    case ORDERED:
                        manager.orderBook(req);
                        break;
                    case RETURNED:
                        manager.returnBook(today, req);
                        break;
                    case PICKED:
                        manager.pickBook(today, req);
                        break;
                    case READ:
                        manager.readBook(today, req);
                        break;
                    case RESTORED:
                        manager.restoreBook(today, req);
                        break;
                    default:
                }
            }
        }
    }
}
