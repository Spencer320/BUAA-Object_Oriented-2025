public class SchedulingState implements ElevatorState {
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
        elevator.setDirection(Elevator.Direction.UP);
        elevator.setCurrentFloor(elevator.getCurrentFloor() + 1);
    }

    @Override
    public void moveDown(Elevator elevator) {
        elevator.setDirection(Elevator.Direction.DOWN);
        elevator.setCurrentFloor(elevator.getCurrentFloor() - 1);
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
        ElevatorTools.formatOutput(ElevatorTools.OutputType.ARRIVE, elevator);
    }

    @Override
    public void scheduleBegin(Elevator elevator) {

    }

    @Override
    public void scheduleEnd(Elevator elevator) {
        ElevatorTools.formatOutput(ElevatorTools.OutputType.END_S, elevator);
        elevator.setState(new WaitingState());
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
