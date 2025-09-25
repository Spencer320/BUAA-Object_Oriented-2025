import java.util.ArrayList;

public class Elevator extends Thread {
    /* start static class */
    static class Constant {
        static final int TopFloor = 7;
        static final int BottomFloor = - 3; // B1 -> 0
        static final int Capacity = 6;
        static final int Speed = 400; // millis per floor
        static final int DSpeed = 200; // millis per floor
        static final int DoorOpenTime = 400; // millis
        static final int StopTime = 1000; // millis
    }

    static class Init {
        static final int floor = 1;
        static final int personInElevator = 0;
        static final Direction direction = Direction.STILL;
        static final ElevatorState state = new WaitingState();
        static final ElevatorType type = ElevatorType.S;
        static final UpdateProcess update = UpdateProcess.N;
    }
    /* end static class */

    /* start attributes */
    enum Direction {
        UP, DOWN, STILL
    }

    enum ElevatorType {
        S, DA, DB
    }

    enum UpdateProcess {
        N, P, I, D // none,pre,ing and done
    }

    private final int elevatorId;
    private ElevatorState state;
    private int personInElevator;
    private int currentFloor;
    private Direction direction;
    private final PassengerSet currentPassenger = new PassengerSet();
    private final PassengerSet waitingPassenger;
    private final RequestTable requestTable; // main request pool
    private int topFloor = Constant.TopFloor;
    private int bottomFloor = Constant.BottomFloor;
    private int speed = Constant.Speed;
    private ElevatorType type;
    private UpdateProcess update;
    private boolean ready = false;
    private boolean free = false;
    private DoubleCarElevatorManager floorResource;

    public Elevator(int id, PassengerSet waitingPassenger, RequestTable requestTable) {
        this.elevatorId = id;
        this.waitingPassenger = waitingPassenger;
        this.state = Init.state;
        this.personInElevator = Init.personInElevator;
        this.currentFloor = Init.floor;
        this.direction = Init.direction;
        this.requestTable = requestTable;
        this.type = Init.type;
        this.update = Init.update;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public ElevatorType getType() {
        return type;
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public int getPersonInElevator() {
        return personInElevator;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getTopFloor() {
        return topFloor;
    }

    public int getBottomFloor() {
        return bottomFloor;
    }

    public PassengerSet getCurrentPassenger() {
        return currentPassenger;
    }

    public PassengerSet getWaitingPassenger() {
        return waitingPassenger;
    }

    public UpdateProcess getUpdate() {
        return update;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isSche() {
        return waitingPassenger.isSche();
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setState(ElevatorState state) {
        this.state = state;
    }
    /* end attributes */

    /* start elevator thread*/
    @Override
    public void run() {
        while (true) {
            while (! ElevatorTools.hasTasks(this) && ! waitingPassenger.isEnd() &&
                ElevatorTools.runnable(this) && state instanceof WaitingState) {
                waitWaitingPassenger();
            }
            if (! ElevatorTools.hasTasks(this) && waitingPassenger.isEnd() &&
                ElevatorTools.runnable(this) && state instanceof WaitingState) {
                break;
            }
            if (state instanceof WorkingState) {
                handleWorkingState();
            } else if (state instanceof WaitingState) {
                handleWaitingState();
            } else if (state instanceof MovingState) {
                handleMovingState();
            } else if (state instanceof SchedulingState) {
                handleSchedulingState();
            } else if (state instanceof UpdatingState) {
                handleUpdatingState();
            } else {
                throw new RuntimeException("Unexpected state");
            }
        }
    }
    /* end elevator thread*/

    /* start methods: state mode*/
    public void doorOpen() {
        state.doorOpen(this);
    }

    public void doorClose() {
        state.doorClose(this);
    }

    public void moveUp() {
        state.moveUp(this);
    }

    public void moveDown() {
        state.moveDown(this);
    }

    public void passengerIn(Passenger passenger) {
        state.passengerIn(this, passenger);
    }

    public void passengerOut(Passenger passenger) {
        state.passengerOut(this, passenger);
    }

    public void arriveFloor() {
        state.arriveFloor(this);
    }

    public void scheduleBegin() {
        state.scheduleBegin(this);
    }

    public void scheduleEnd() {
        state.scheduleEnd(this);
    }

    public void updatePrepare() {
        state.updatePrepare(this);
    }

    public void updateReady() {
        state.updateReady(this);
    }

    public void updateDone() {
        state.updateDone(this);
    }
    /* end methods: state mode*/

    /* start methods: strategy mode*/
    private void handleWorkingState() {
        sleepTime(Constant.DoorOpenTime);
        if (ElevatorTools.atFloor(this)) {
            clearCurrentPassenger();
        } else {
            processPassengerExit();
        }
        updateDirection();
        processPassengerEntry();
        doorClose();
    }

    private void handleWaitingState() {
        if (isSche()) {
            scheduleBegin();
            return;
        } else if (getUpdate() == UpdateProcess.P) {
            updatePrepare();
            return;
        }
        if (ElevatorTools.hasPassengerToExit(this)) {
            doorOpen();
        } else if (ElevatorTools.hasPassengerToEnter(this)) {
            updateDirection();
            doorOpen();
        } else {
            if (direction == Direction.UP && ElevatorTools.hasRequestsAbove(this)) {
                moveUp();
            } else if (direction == Direction.DOWN && ElevatorTools.hasRequestsBelow(this)) {
                moveDown();
            } else if (direction == Direction.STILL) {
                if (ElevatorTools.hasRequestsAbove(this)) {
                    setDirection(Direction.UP);
                    moveUp();
                } else if (ElevatorTools.hasRequestsBelow(this)) {
                    setDirection(Direction.DOWN);
                    moveDown();
                }
            } else if (ElevatorTools.atFloor(this)) {
                leaveFloor();
            } else {
                setDirection(Direction.STILL); // No tasks in the current direction
            }
        }
    }

    private void handleMovingState() {
        if (ElevatorTools.atFloor(this)) {
            floorResource.get();
        }
        sleepTime(speed / 2);
        if (free) {
            floorResource.release();
            setFree(false);
        }
        sleepTime(speed / 2);
        arriveFloor();
    }

    private void handleSchedulingState() {
        clearWaitingPassenger();
        speed = waitingPassenger.getScheSpeed();
        int targetFloor = waitingPassenger.getScheFloor();
        int direction = targetFloor - currentFloor;
        if (direction > 0) {
            while (currentFloor != targetFloor) {
                moveUp();
                sleepTime(speed);
                arriveFloor();
            }
        } else if (direction < 0) {
            while (currentFloor != targetFloor) {
                moveDown();
                sleepTime(speed);
                arriveFloor();
            }
        }
        doorOpen();
        clearCurrentPassenger();
        speed = Constant.Speed;
        sleepTime(Constant.StopTime);
        doorClose();
        scheduleEnd();
        waitingPassenger.setSche(false);
        notifyRequestTable();
    }

    private void handleUpdatingState() {
        switch (update) {
            case P:
                if (personInElevator != 0) {
                    doorOpen();
                    sleepTime(Constant.DoorOpenTime);
                    clearCurrentPassenger();
                    doorClose();
                }
                updateReady();
                waitWaitingPassenger();
                break;
            case I:
                clearWaitingPassenger();
                if (type == ElevatorType.DA) {
                    setCurrentFloor(bottomFloor + 1);
                } else if (type == ElevatorType.DB) {
                    setCurrentFloor(topFloor - 1);
                }
                waitWaitingPassenger();
                break;
            case D:
                updateDone();
                notifyRequestTable();
                break;
            default:
        }
    }
    /* end methods: strategy mode*/

    /* start aided methods: state mode*/
    public void personIn(Passenger passenger) {
        personInElevator += 1;
        currentPassenger.add(passenger);
        waitingPassenger.remove(passenger);
        ElevatorTools.formatOutput(ElevatorTools.OutputType.IN, passenger, this);
    }

    public void personOut(Passenger passenger) {
        personInElevator -= 1;
        currentPassenger.remove(passenger);
        if (currentFloor == passenger.getToFloor()) {
            ElevatorTools.formatOutput(ElevatorTools.OutputType.OUT_S, passenger, this);
        } else {
            ElevatorTools.formatOutput(ElevatorTools.OutputType.OUT_F, passenger, this);
        }
    }

    private void leaveFloor() {
        if (type == ElevatorType.DA) {
            moveUp();
        } else if (type == ElevatorType.DB) {
            moveDown();
        }
    }
    /* end aided methods: state mode*/

    /* start aided methods: strategy mode*/
    private void processPassengerExit() {
        ArrayList<Passenger> outList = new ArrayList<>();
        for (Passenger p : currentPassenger.getPassengerSet()) {
            if (p.getToFloor() == currentFloor) {
                outList.add(p);
            }
        }
        for (Passenger p : outList) {
            passengerOut(p);
        }
    }

    private void processPassengerEntry() {
        ArrayList<Passenger> inList = new ArrayList<>();
        for (Passenger p : waitingPassenger.getPassengerSet()) {
            if (p.getFromFloor() == currentFloor) {
                if (direction == p.getDirection()) {
                    inList.add(p);
                }
            }
        }
        int remainingCapacity = Constant.Capacity - personInElevator;
        for (int i = 0; i < Math.min(remainingCapacity, inList.size()); i++) {
            passengerIn(inList.get(i));
        }
    }

    private void updateDirection() {
        if (personInElevator == 0) {
            for (Passenger p : waitingPassenger.getPassengerSet()) {
                if (p.getFromFloor() == currentFloor) {
                    setDirection(p.getDirection());
                    break;
                }
            }
        }
    }

    public void clearWaitingPassenger() {
        synchronized (waitingPassenger) {
            for (Passenger passenger : waitingPassenger.getPassengerSet()) {
                ElevatorRequest request = new ElevatorRequest(new Passenger(passenger));
                requestTable.offer(request);
            }
            waitingPassenger.clear();
        }
    }

    public void clearCurrentPassenger() {
        ArrayList<Passenger> copy = new ArrayList<>();
        synchronized (currentPassenger) {
            for (Passenger passenger : currentPassenger.getPassengerSet()) {
                copy.add(passenger);
            }
            for (Passenger passenger : copy) {
                passengerOut(passenger);
                if (passenger.getToFloor() != currentFloor) {
                    ElevatorRequest request = new ElevatorRequest(new Passenger(
                        passenger.getId(), currentFloor, passenger.getToFloor(),
                        passenger.getPriority()));
                    requestTable.offer(request);
                }
            }
        }
        personInElevator = 0;
    }

    public void setUpdateP(ElevatorType type, Integer transferFloor) {
        this.update = UpdateProcess.P;
        this.type = type;
        if (type == ElevatorType.DA) {
            bottomFloor = transferFloor; // above car
        } else if (type == ElevatorType.DB) {
            topFloor = transferFloor; // below car
        }
        notifyWaitingPassenger();
    }

    public void setUpdateI() {
        this.update = UpdateProcess.I;
        notifyWaitingPassenger();
    }

    public void setUpdateD(DoubleCarElevatorManager resource) {
        this.update = UpdateProcess.D;
        this.speed = Constant.DSpeed;
        floorResource = resource;
        notifyWaitingPassenger();
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public void setFree(boolean free) {
        this.free = free;
    }
    /* end aided methods: strategy mode*/

    /* start control methods*/
    private void waitWaitingPassenger() {
        synchronized (waitingPassenger) {
            try {
                waitingPassenger.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void notifyWaitingPassenger() {
        synchronized (waitingPassenger) {
            waitingPassenger.notifyAll();
        }
    }

    private void notifyRequestTable() {
        synchronized (requestTable) {
            requestTable.notifyAll();
        }
    }

    private void sleepTime(long time) {
        try {
            sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    /* end control methods*/
}