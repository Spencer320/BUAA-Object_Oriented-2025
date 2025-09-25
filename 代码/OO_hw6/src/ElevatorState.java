public interface ElevatorState {
    void doorOpen(Elevator elevator);

    void doorClose(Elevator elevator);

    void moveUp(Elevator elevator);

    void moveDown(Elevator elevator);

    void passengerIn(Elevator elevator, Passenger passenger);

    void passengerOut(Elevator elevator, Passenger passenger);

    void arriveFloor(Elevator elevator);

    void scheduleBegin(Elevator elevator);

    void scheduleEnd(Elevator elevator);

}
