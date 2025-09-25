public class Passenger {
    private final int id;
    private final int fromFloor;
    private final int toFloor;
    private final int priority;
    private final Elevator.Direction direction;

    public Passenger(int id, int fromFloor, int toFloor, int priority) {
        this.id = id;
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
        this.priority = priority;
        this.direction = (toFloor - fromFloor) > 0 ? Elevator.Direction.UP :
            Elevator.Direction.DOWN;
    }

    public Passenger(Passenger p) {
        this.id = p.getId();
        this.fromFloor = p.getFromFloor();
        this.toFloor = p.getToFloor();
        this.priority = p.getPriority();
        this.direction = p.getDirection();

    }

    public int getId() {
        return id;
    }

    public int getFromFloor() {
        return fromFloor;
    }

    public int getToFloor() {
        return toFloor;
    }

    public int getPriority() {
        return priority;
    }

    public Elevator.Direction getDirection() {
        return direction;
    }

}