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
        elevator.formatOutput(Elevator.OutputType.ARRIVE);
        elevator.setState(new WaitingState());
    }
}
