public class MovingState implements ElevatorState {
    @Override
    public void doorOpen(Elevator elevator) {
    }

    @Override
    public void doorClose(Elevator elevator) {
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
    }

    @Override
    public void arriveFloor(Elevator elevator) {
        ElevatorTools.formatOutput(ElevatorTools.OutputType.ARRIVE,elevator);
        elevator.setState(new WaitingState());
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
