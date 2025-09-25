public class SchedulingState implements ElevatorState {
    @Override
    public void doorOpen(Elevator elevator) {
        elevator.formatOutput(Elevator.OutputType.OPEN);
    }

    @Override
    public void doorClose(Elevator elevator) {
        elevator.formatOutput(Elevator.OutputType.CLOSE);
    }

    @Override
    public void moveUp(Elevator elevator) {
        elevator.floorUp();
    }

    @Override
    public void moveDown(Elevator elevator) {
        elevator.floorDown();
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
        elevator.formatOutput(Elevator.OutputType.ARRIVE);
    }

    @Override
    public void scheduleBegin(Elevator elevator) {

    }

    @Override
    public void scheduleEnd(Elevator elevator) {
        elevator.formatOutput(Elevator.OutputType.END);
        elevator.setState(new WaitingState());
    }
}
