public class WorkingState implements ElevatorState {
    @Override
    public void doorOpen(Elevator elevator) {
    }

    @Override
    public void doorClose(Elevator elevator) {
        ElevatorTools.formatOutput(ElevatorTools.OutputType.CLOSE, elevator);
        elevator.setState(new WaitingState());
    }

    @Override
    public void moveUp(Elevator elevator) {
    }

    @Override
    public void moveDown(Elevator elevator) {
    }

    @Override
    public void passengerIn(Elevator elevator, Passenger passenger) {
        if (elevator.getPersonInElevator() < Elevator.Constant.Capacity) {
            elevator.personIn(passenger);
        } else {
            throw new RuntimeException("Unexpected action");
        }
    }

    @Override
    public void passengerOut(Elevator elevator, Passenger passenger) {
        if (elevator.getPersonInElevator() > 0) {
            elevator.personOut(passenger);
        } else {
            throw new RuntimeException("Unexpected action");
        }
    }

    @Override
    public void arriveFloor(Elevator elevator) {

    }

    @Override
    public void scheduleBegin(Elevator elevator) {

    }

    @Override
    public void scheduleEnd(Elevator elevator) {

    }

    @Override
    public void updatePrepare(Elevator elevator) {

    }

    @Override
    public void updateReady(Elevator elevator) {

    }

    @Override
    public void updateDone(Elevator elevator) {

    }
}
