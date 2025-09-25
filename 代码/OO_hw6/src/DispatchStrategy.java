import java.util.ArrayList;

public class DispatchStrategy {
    private ArrayList<Elevator> elevators;

    enum Delay {
        IGNORE,LOW, HIGH, WAIT
    }

    public DispatchStrategy(ArrayList<Elevator> elevators) {
        this.elevators = elevators;
    }

    public int getElevatorId(Passenger p) {
        int minScore = 0;
        int index = 0;
        int score;
        boolean first = true;
        for (Elevator elevator : elevators) {
            if (elevator.isSche()) {
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

        // weight score
        int weight = e.getPersonInElevator() + e.getWaitingPassengerSize();
        if (weight < Elevator.Constant.Capacity) {
            return score;
        } else if (weight < 2 * Elevator.Constant.Capacity) {
            return 2 * score;
        } else {
            return 10 * score;
        }
    }

    public Delay getDelay() {
        int count = 0;
        for (Elevator elevator : elevators) {
            if (elevator.isSche()) {
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

}
