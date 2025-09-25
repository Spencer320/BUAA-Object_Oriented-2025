public class WaitingState implements ElevatorState {
    @Override
    public void doorOpen(Elevator elevator) {
        elevator.formatOutput(Elevator.OutputType.OPEN);
        elevator.setState(new WorkingState());
    }

    @Override
    public void doorClose(Elevator elevator) {
    }

    @Override
    public void moveUp(Elevator elevator) {
        if (elevator.getCurrentFloor() < Elevator.Constant.TopFloor) {
            elevator.floorUp();
        } else {
            throw new RuntimeException("Unexpected action");
        }
        elevator.setState(new MovingState());
    }

    @Override
    public void moveDown(Elevator elevator) {
        if (elevator.getCurrentFloor() > Elevator.Constant.BottomFloor) {
            elevator.floorDown();
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
}
