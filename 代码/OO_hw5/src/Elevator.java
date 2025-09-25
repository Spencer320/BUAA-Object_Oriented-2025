import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;

public class Elevator extends Thread {
    /* start static class */
    static class Constant {
        static final int TopFloor = 7;
        static final int BottomFloor = - 3; // B1 -> 0
        static final int Capacity = 6;
        static final int Speed = 400; // millis per floor
        static final int DoorOpenTime = 400; // millis
    }

    static class Init {
        static final int floor = 1;
        static final int personInElevator = 0;
        static final Direction direction = Direction.STILL;
        static final ElevatorState state = new WaitingState();
    }

    /* end static class */

    /* start attributes */
    enum Direction {
        UP, DOWN, STILL
    }

    enum OutputType {
        OPEN, CLOSE, ARRIVE, IN, OUT
    }

    private final int elevatorId;
    private ElevatorState state;
    private int personInElevator;
    private int currentFloor;
    private Direction direction;
    private final PassengerSet currentPassenger = new PassengerSet();
    private final PassengerSet waitingPassenger;

    public Elevator(int id, PassengerSet waitingPassenger) {
        this.elevatorId = id;
        this.waitingPassenger = waitingPassenger;
        this.state = Init.state;
        this.personInElevator = Init.personInElevator;
        this.currentFloor = Init.floor;
        this.direction = Init.direction;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public int getPersonInElevator() {
        return personInElevator;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setState(ElevatorState state) {
        this.state = state;
    }

    public static int floorToNumber(String floor) {
        if (floor.length() != 2) {
            throw new IllegalArgumentException("Invalid request");
        }
        if (floor.charAt(0) == 'F') {
            return (int) floor.charAt(1) - '0';
        } else if (floor.charAt(0) == 'B') {
            return - ((int) floor.charAt(1) - '0') + 1;
        } else {
            throw new IllegalArgumentException("Invalid request");
        }
    }

    public static String floorToString(int floor) {
        if (floor > 0) {
            return "F" + floor;
        } else {
            int f = - floor + 1;
            return "B" + f;
        }
    }
    /* end attributes */

    /* start elevator thread*/
    @Override
    public void run() {
        while (true) {
            while (! hasTasks() && ! waitingPassenger.isEnd()) {
                synchronized (waitingPassenger) {
                    try {
                        // System.out.println("Elevator " + this.elevatorId + " is waiting...");
                        waitingPassenger.waitForPassenger();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (waitingPassenger.isEnd() &&
                currentPassenger.isEmpty() && waitingPassenger.isEmpty()) {
                // System.out.println("Elevator " + this.elevatorId + " is ended.");
                break;
            }
            if (state instanceof WorkingState) {
                handleWorkingState();
            } else if (state instanceof WaitingState) {
                handleWaitingState();
            } else if (state instanceof MovingState) {
                handleMovingState();
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
    /* end methods: state mode*/

    /* start methods: strategy mode*/
    private void handleWorkingState() {
        // assure out/in needed
        try {
            sleep(Constant.DoorOpenTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        processPassengerExit();
        updateDirection();
        processPassengerEntry();
        doorClose();
    }

    private void handleWaitingState() {
        // assure tasks existed
        if (hasPassengerToExit()) {
            doorOpen();
        } else if (hasPassengerToEnter()) {
            updateDirection();
            doorOpen();
        } else {
            if (direction == Direction.UP && hasRequestsAbove()) {
                moveUp();
            } else if (direction == Direction.DOWN && hasRequestsBelow()) {
                moveDown();
            } else if (direction == Direction.STILL) {
                if (hasRequestsAbove()) {
                    setDirection(Direction.UP);
                    moveUp();
                } else if (hasRequestsBelow()) {
                    setDirection(Direction.DOWN);
                    moveDown();
                }
            } else {
                setDirection(Direction.STILL); // No tasks in the current direction
            }
        }
    }

    private void handleMovingState() {
        try {
            sleep(Constant.Speed);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        arriveFloor();
    }
    /* end methods: strategy mode*/

    /* start aided methods: state mode*/
    public void floorUp() {
        setDirection(Elevator.Direction.UP);
        currentFloor += 1;
    }

    public void floorDown() {
        setDirection(Direction.DOWN);
        currentFloor -= 1;
    }

    public void personIn(Passenger passenger) {
        personInElevator += 1;
        currentPassenger.add(passenger);
        waitingPassenger.remove(passenger);
        formatOutput(OutputType.IN, passenger);
    }

    public void personOut(Passenger passenger) {
        personInElevator -= 1;
        currentPassenger.remove(passenger);
        formatOutput(OutputType.OUT, passenger);
    }

    public void formatOutput(OutputType type) {
        switch (type) {
            case OPEN:
                TimableOutput.println("OPEN-" +
                    floorToString(currentFloor) + "-" + getElevatorId());
                break;
            case CLOSE:
                TimableOutput.println("CLOSE-" +
                    floorToString(currentFloor) + "-" + getElevatorId());
                break;
            case ARRIVE:
                TimableOutput.println("ARRIVE-" +
                    floorToString(currentFloor) + "-" + getElevatorId());
                break;
            default:
        }
    }

    public void formatOutput(OutputType type, Passenger p) {
        switch (type) {
            case IN:
                TimableOutput.println("IN-" +
                    p.getId() + "-" + floorToString(currentFloor) + "-" + getElevatorId());
                break;
            case OUT:
                TimableOutput.println("OUT-" +
                    p.getId() + "-" + floorToString(currentFloor) + "-" + getElevatorId());
                break;
            default:
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

    private boolean hasTasks() {
        return hasPassengerToExit() || hasPassengerToEnter() ||
            hasRequestsAbove() || hasRequestsBelow();
    }

    private boolean hasPassengerToExit() {
        for (Passenger p : currentPassenger.getPassengerSet()) {
            if (p.getToFloor() == currentFloor) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPassengerToEnter() {
        if (personInElevator >= Constant.Capacity) {
            return false;
        }
        for (Passenger p : waitingPassenger.getPassengerSet()) {
            if (p.getFromFloor() == currentFloor) {
                int passengerDirection = p.getToFloor() - p.getFromFloor();
                if (direction == Direction.STILL ||
                    (direction == Direction.UP && passengerDirection > 0) ||
                    (direction == Direction.DOWN && passengerDirection < 0)) {
                    return true;
                }
                if (personInElevator == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasRequestsAbove() {
        for (Passenger p : currentPassenger.getPassengerSet()) {
            if (p.getToFloor() > currentFloor) {
                return true;
            }
        }
        for (Passenger p : waitingPassenger.getPassengerSet()) {
            if (p.getFromFloor() > currentFloor) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRequestsBelow() {
        for (Passenger p : currentPassenger.getPassengerSet()) {
            if (p.getToFloor() < currentFloor) {
                return true;
            }
        }
        for (Passenger p : waitingPassenger.getPassengerSet()) {
            if (p.getFromFloor() < currentFloor) {
                return true;
            }
        }
        return false;
    }
    /* end aided methods: strategy mode*/
}







