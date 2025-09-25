import com.oocourse.elevator3.TimableOutput;

public class ElevatorTools {
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

    enum OutputType {
        OPEN, CLOSE, ARRIVE, IN, OUT_F, OUT_S, BEGIN_S, END_S, RECEIVE, BEGIN_U, END_U
    }

    public static void formatOutput(OutputType type, Elevator elevator) {
        switch (type) {
            case OPEN:
                TimableOutput.println("OPEN-" +
                    ElevatorTools.floorToString(elevator.getCurrentFloor()) +
                    "-" + elevator.getElevatorId());
                break;
            case CLOSE:
                TimableOutput.println("CLOSE-" +
                    ElevatorTools.floorToString(elevator.getCurrentFloor()) +
                    "-" + elevator.getElevatorId());
                break;
            case ARRIVE:
                TimableOutput.println("ARRIVE-" +
                    ElevatorTools.floorToString(elevator.getCurrentFloor()) +
                    "-" + elevator.getElevatorId());
                break;
            case BEGIN_S:
                TimableOutput.println("SCHE-BEGIN-" + elevator.getElevatorId());
                break;
            case END_S:
                TimableOutput.println("SCHE-END-" + elevator.getElevatorId());
                break;
            default:
        }
    }

    public static void formatOutput(OutputType type, Passenger p, Elevator elevator) {
        switch (type) {
            case IN:
                TimableOutput.println("IN-" +
                    p.getId() + "-" + ElevatorTools.floorToString(elevator.getCurrentFloor()) +
                    "-" + elevator.getElevatorId());
                break;
            case OUT_F:
                TimableOutput.println("OUT-F-" +
                    p.getId() + "-" + ElevatorTools.floorToString(elevator.getCurrentFloor()) +
                    "-" + elevator.getElevatorId());
                break;
            case OUT_S:
                TimableOutput.println("OUT-S-" +
                    p.getId() + "-" + ElevatorTools.floorToString(elevator.getCurrentFloor()) +
                    "-" + elevator.getElevatorId());
                break;
            default:
        }
    }

    public static void formatOutput(OutputType type, Passenger p, Integer elevatorId) {
        switch (type) {
            case RECEIVE:
                TimableOutput.println("RECEIVE-" + p.getId() + "-" + elevatorId);
                break;
            default:
        }
    }

    public static void formatOutput(OutputType type, Integer elevatorAId, Integer elevatorBId) {
        switch (type) {
            case BEGIN_U:
                TimableOutput.println("UPDATE-BEGIN-" + elevatorAId + "-" + elevatorBId);
                break;
            case END_U:
                TimableOutput.println("UPDATE-END-" + elevatorAId + "-" + elevatorBId);
                break;
            default:
        }
    }

    public static boolean hasTasks(Elevator elevator) {
        return hasPassengerToExit(elevator) ||
            hasPassengerToEnter(elevator) ||
            hasRequestsAbove(elevator) ||
            hasRequestsBelow(elevator) ||
            atFloor(elevator);
    }

    public static boolean hasPassengerToExit(Elevator elevator) {
        for (Passenger p : elevator.getCurrentPassenger().getPassengerSet()) {
            if (p.getToFloor() == elevator.getCurrentFloor()) {
                return true;
            }
            if (atFloor(elevator) && ! ElevatorTools.floorInRange(elevator, p.getToFloor())) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasPassengerToEnter(Elevator elevator) {
        if (elevator.getPersonInElevator() == Elevator.Constant.Capacity) {
            return false;
        }
        for (Passenger p : elevator.getWaitingPassenger().getPassengerSet()) {
            if (p.getFromFloor() == elevator.getCurrentFloor()) {
                int directionP = p.getToFloor() - p.getFromFloor();
                if (elevator.getDirection() == Elevator.Direction.STILL ||
                    (elevator.getDirection() == Elevator.Direction.UP && directionP > 0) ||
                    (elevator.getDirection() == Elevator.Direction.DOWN && directionP < 0)) {
                    return true;
                }
                if (elevator.getPersonInElevator() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasRequestsAbove(Elevator elevator) {
        for (Passenger p : elevator.getCurrentPassenger().getPassengerSet()) {
            if (Math.min(p.getToFloor(), elevator.getTopFloor()) > elevator.getCurrentFloor()) {
                return true;
            }
        }
        for (Passenger p : elevator.getWaitingPassenger().getPassengerSet()) {
            if (p.getFromFloor() > elevator.getCurrentFloor()) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasRequestsBelow(Elevator elevator) {
        for (Passenger p : elevator.getCurrentPassenger().getPassengerSet()) {
            if (Math.max(p.getToFloor(), elevator.getBottomFloor()) < elevator.getCurrentFloor()) {
                return true;
            }
        }
        for (Passenger p : elevator.getWaitingPassenger().getPassengerSet()) {
            if (p.getFromFloor() < elevator.getCurrentFloor()) {
                return true;
            }
        }
        return false;
    }

    public static boolean atFloor(Elevator elevator) {
        if (elevator.getUpdate() != Elevator.UpdateProcess.D) {
            return false;
        }
        Elevator.ElevatorType type = elevator.getType();
        int floor = elevator.getCurrentFloor();
        return (type == Elevator.ElevatorType.DA && floor == elevator.getBottomFloor()) ||
            (type == Elevator.ElevatorType.DB && floor == elevator.getTopFloor());
    }

    public static boolean floorInRange(Elevator elevator, Integer floor) {
        return (elevator.getBottomFloor() <= floor) && (elevator.getTopFloor() >= floor);
    }

    public static boolean floorAtBorder(Elevator elevator, Integer floor) {
        Elevator.ElevatorType type = elevator.getType();
        return (type == Elevator.ElevatorType.DA && floor == elevator.getBottomFloor()) ||
            (type == Elevator.ElevatorType.DB && floor == elevator.getTopFloor());
    }

    public static boolean runnable(Elevator elevator) {
        return ! elevator.isSche() &&
            ! (elevator.getUpdate() == Elevator.UpdateProcess.P) &&
            ! (elevator.getUpdate() == Elevator.UpdateProcess.I);
    }
}
