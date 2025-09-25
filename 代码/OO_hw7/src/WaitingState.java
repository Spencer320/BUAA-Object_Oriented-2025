public class WaitingState implements ElevatorState {
    @Override
    public void doorOpen(Elevator elevator) {
        ElevatorTools.formatOutput(ElevatorTools.OutputType.OPEN, elevator);
        elevator.setState(new WorkingState());
    }

    @Override
    public void doorClose(Elevator elevator) {
    }

    @Override
    public void moveUp(Elevator elevator) {
        if (elevator.getCurrentFloor() < elevator.getTopFloor()) {
            if (ElevatorTools.atFloor(elevator)) {
                elevator.setFree(true);
            }
            elevator.setDirection(Elevator.Direction.UP);
            elevator.setCurrentFloor(elevator.getCurrentFloor() + 1);
        } else {
            throw new RuntimeException("Unexpected action");
        }
        elevator.setState(new MovingState());
    }

    @Override
    public void moveDown(Elevator elevator) {
        if (elevator.getCurrentFloor() > elevator.getBottomFloor()) {
            if (ElevatorTools.atFloor(elevator)) {
                elevator.setFree(true);
            }
            elevator.setDirection(Elevator.Direction.DOWN);
            elevator.setCurrentFloor(elevator.getCurrentFloor() - 1);
        } else {
            throw new RuntimeException("Unexpected action");
        }
        elevator.setState(new MovingState());
    }

    @Override
    public void passengerIn(Elevator elevator, Passenger passenger) {
    }

    @Override
    public void passengerOut(Elevator elevator, Passenger passenger) {
    }

    @Override
    public void arriveFloor(Elevator elevator) {
    }

    @Override
    public void scheduleBegin(Elevator elevator) {
        ElevatorTools.formatOutput(ElevatorTools.OutputType.BEGIN_S, elevator);
        elevator.setState(new SchedulingState());
    }

    @Override
    public void scheduleEnd(Elevator elevator) {

    }

    @Override
    public void updatePrepare(Elevator elevator) {
        elevator.setState(new UpdatingState());
    }

    @Override
    public void updateReady(Elevator elevator) {

    }

    @Override
    public void updateDone(Elevator elevator) {

    }
}
