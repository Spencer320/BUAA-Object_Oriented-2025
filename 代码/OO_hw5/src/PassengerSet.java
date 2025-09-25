import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PassengerSet {
    private Set<Passenger> passengerSet = ConcurrentHashMap.newKeySet();
    private boolean isEnd = false;

    public synchronized void add(Passenger passenger) {
        passengerSet.add(passenger);
        notifyAll();
    }

    public synchronized void remove(Passenger passenger) {
        passengerSet.remove(passenger);
        notifyAll();
    }

    public synchronized void setEnd() {
        isEnd = true;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        return isEnd;
    }

    public synchronized boolean isEmpty() {
        return passengerSet.isEmpty();
    }

    public synchronized Set<Passenger> getPassengerSet() {
        return passengerSet;
    }

    public synchronized void waitForPassenger() throws InterruptedException {
        wait();
    }
}
