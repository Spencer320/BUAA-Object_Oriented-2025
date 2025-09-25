import com.oocourse.elevator1.Request;
import java.util.ArrayList;

public class RequestTable {
    private final ArrayList<Request> requests = new ArrayList<>();
    private boolean isEnd = false;

    public synchronized void offer(Request request) {
        requests.add(request);
        notifyAll();
    }

    public synchronized Request poll() {
        if (requests.isEmpty() && !isEnd()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (requests.isEmpty()) {
            return null;
        }
        notifyAll();
        return requests.remove(0);
    }

    public synchronized void setEnd() {
        isEnd = true;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        notifyAll();
        return isEnd;
    }

    public synchronized boolean isEmpty() {
        notifyAll();
        return requests.isEmpty();
    }
}
