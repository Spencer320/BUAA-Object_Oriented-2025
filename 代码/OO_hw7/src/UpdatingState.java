public class UpdatingState implements ElevatorState {
    @Override
    public void doorOpen(Elevator elevator) {
        ElevatorTools.formatOutput(ElevatorTools.OutputType.OPEN, elevator);
    }

    @Override
    public void doorClose(Elevator elevator) {
        ElevatorTools.formatOutput(ElevatorTools.OutputType.CLOSE, elevator);
    }

    @Override
    public void moveUp(Elevator elevator) {

    }

    @Override
    public void moveDown(Elevator elevator) {

    }

    @Override
    public void passengerIn(Elevator elevator, Passenger passenger) {

    }

    @Override
    public void passengerOut(Elevator elevator, Passenger passenger) {
        elevator.personOut(passenger);
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
        elevator.setReady(true);
    }

    @Override
    public void updateDone(Elevator elevator) {
        elevator.setState(new WaitingState());
    }
}
