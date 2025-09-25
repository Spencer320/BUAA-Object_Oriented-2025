import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class DispatchStrategy {
    private ArrayList<Elevator> elevators;

    enum Delay {
        IGNORE, LOW, HIGH, WAIT
    }

    public DispatchStrategy(ArrayList<Elevator> elevators) {
        this.elevators = elevators;
    }

    public int getElevatorId(Passenger p) {
        int minScore = 0;
        int index = 0;
        int wait = 0;
        int score;
        do {
            boolean first = true;
            try {
                sleep(wait);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (Elevator elevator : elevators) {
                if (! ElevatorTools.runnable(elevator)) {
                    continue;
                }
                if (! ElevatorTools.floorInRange(elevator, p.getFromFloor())) {
                    continue;
                }
                if ((ElevatorTools.floorAtBorder(elevator, p.getFromFloor()) &&
                    ! ElevatorTools.floorInRange(elevator, p.getToFloor()))) {
                    continue;
                }
                score = getElevatorScore(elevator, p);
                if (first) {
                    minScore = score;
                    index = elevator.getElevatorId();
                    first = false;
                } else if (score < minScore) {
                    minScore = score;
                    index = elevator.getElevatorId();
                }
            }
            wait = 100 + wait * 2;
        } while (index == 0);
        return index;
    }

    public int getElevatorScore(Elevator e, Passenger p) {
        int score = 1;
        int elevatorFloor = e.getCurrentFloor();
        int passengerFloor = p.getFromFloor();
        Elevator.Direction elevatorDirection = e.getDirection();
        Elevator.Direction passengerDirection = p.getDirection();

        // distance score
        if (elevatorDirection == Elevator.Direction.STILL) {
            // elevator still
            score += Math.abs(elevatorFloor - passengerFloor);
        } else if (elevatorDirection == passengerDirection) {
            // direction matched
            if ((elevatorDirection == Elevator.Direction.UP && elevatorFloor <= passengerFloor) ||
                (elevatorDirection == Elevator.Direction.DOWN && elevatorFloor >= passengerFloor)) {
                // floor matched
                score += Math.abs(elevatorFloor - passengerFloor);
            } else {
                // floor unmatched
                score += Elevator.Constant.TopFloor - Elevator.Constant.BottomFloor;
            }
        } else {
            // direction unmatched
            if (elevatorDirection == Elevator.Direction.UP) {
                score += Elevator.Constant.TopFloor - Math.min(elevatorFloor, passengerFloor);
            } else {
                score += Math.max(elevatorFloor, passengerFloor) - Elevator.Constant.BottomFloor;
            }
        }

        // weight punish
        int weight = e.getPersonInElevator() + e.getWaitingPassenger().getPassengerSet().size();
        if (weight < Elevator.Constant.Capacity) {
            score *= 1;
        } else if (weight < 2 * Elevator.Constant.Capacity) {
            score *= 2;
        } else {
            score *= 10;
        }

        // speed punish
        if (e.getType() == Elevator.ElevatorType.S) {
            return 2 * score;
        } else if (ElevatorTools.floorInRange(e, p.getToFloor())) {
            return score;
        } else {
            return score + 5;
        }
    }

    public Delay getDelay() {
        int count = 0;
        for (Elevator elevator : elevators) {
            if (! ElevatorTools.runnable(elevator)) {
                count++;
            }
        }
        switch (count) {
            case 0:
            case 1:
                return Delay.IGNORE; // 10 millis
            case 2:
            case 3:
                return Delay.LOW; // 400 millis
            case 4:
            case 5:
                return Delay.HIGH; // 400 millis, 5 times
            case 6:
            default:
                return Delay.WAIT; // wait!
        }
    }

    public void setUpdate(Integer idA, Integer idB, Integer transferFloor) {
        // create a manager
        Elevator elevatorA = null;
        Elevator elevatorB = null;
        for (Elevator elevator : elevators) {
            if (elevator.getElevatorId() == idA) {
                elevatorA = elevator;
            }
            if (elevator.getElevatorId() == idB) {
                elevatorB = elevator;
            }
        }
        DoubleCarElevatorManager manager = new DoubleCarElevatorManager(
            elevatorA, elevatorB, transferFloor);
        manager.start();
    }

    public boolean checkFloorInRange() {
        for (Elevator elevator : elevators) {
            for (Passenger passenger : elevator.getCurrentPassenger().getPassengerSet()) {
                if (! ElevatorTools.floorInRange(elevator, passenger.getToFloor())) {
                    return false;
                }
            }
            for (Passenger passenger : elevator.getWaitingPassenger().getPassengerSet()) {
                if (! ElevatorTools.floorInRange(elevator, passenger.getToFloor())) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkElevatorRunnable() {
        for (Elevator elevator : elevators) {
            if (! ElevatorTools.runnable(elevator)) {
                return false;
            }
        }
        return true;
    }
}
