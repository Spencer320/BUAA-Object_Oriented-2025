import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PassengerSet {
    private Set<Passenger> passengerSet = ConcurrentHashMap.newKeySet();
    private boolean isSche = false;
    private int scheSpeed;
    private int scheFloor;
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

    public synchronized void clear() {
        passengerSet.clear();
    }

    public synchronized Set<Passenger> getPassengerSet() {
        return passengerSet;
    }

    public synchronized boolean isSche() {
        return isSche;
    }

    public synchronized void setSche(boolean sche, int speed, int floor) {
        this.isSche = sche;
        this.scheSpeed = speed;
        this.scheFloor = floor;
        notifyAll();
    }

    public synchronized void setSche(boolean sche) {
        this.isSche = sche;
    }

    public int getScheSpeed() {
        return scheSpeed;
    }

    public int getScheFloor() {
        return scheFloor;
    }
}
