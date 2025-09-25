import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.ScheRequest;
import com.oocourse.elevator3.UpdateRequest;

import java.util.Comparator;
import java.util.PriorityQueue;

public class RequestTable {
    static class RequestComparator implements Comparator<Request> {
        @Override
        public int compare(Request o1, Request o2) {
            // type first
            boolean type1 = (o1 instanceof ScheRequest || o1 instanceof UpdateRequest);
            boolean type2 = (o2 instanceof ScheRequest || o2 instanceof UpdateRequest);
            if (type1 && (! type2)) {
                return - 1;
            } else if ((! type1) && type2) {
                return 1;
            } else if (type1 && type2) {
                return 0;
            }

            // compare priority
            int p1 = 0;
            if (o1 instanceof ElevatorRequest) {
                p1 = ((ElevatorRequest) o1).getElevatorRequest().getPriority();
            } else if (o1 instanceof PersonRequest) {
                p1 = ((PersonRequest) o1).getPriority();
            }
            int p2 = 0;
            if (o2 instanceof ElevatorRequest) {
                p2 = ((ElevatorRequest) o2).getElevatorRequest().getPriority();
            } else if (o2 instanceof PersonRequest) {
                p2 = ((PersonRequest) o2).getPriority();
            }
            // if p1.gtr(p2) , then return a neg_value
            return Integer.compare(p2, p1);
        }
    }

    private final PriorityQueue<Request> requests = new PriorityQueue<>(new RequestComparator());
    private boolean isInputEnd = false;
    private boolean isResendEnd = false;

    public synchronized void offer(Request request) {
        requests.offer(request);
        notifyAll();
    }

    public synchronized Request poll() {
        if (requests.isEmpty() && ! isEnd()) {
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
        return requests.poll();
    }

    public synchronized void setInputEnd() {
        isInputEnd = true;
        notifyAll();
    }

    public synchronized void setResendEnd() {
        isResendEnd = true;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        return isResendEnd && isInputEnd;
    }

    public boolean isInputEnd() {
        return isInputEnd;
    }

    public synchronized boolean isEmpty() {
        return requests.isEmpty();
    }

    public synchronized boolean isScheEmpty() {
        for (Request request : requests) {
            if (request instanceof ScheRequest) {
                return false;
            }
        }
        return true;
    }
}
