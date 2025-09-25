import com.oocourse.elevator2.Request;

public class ElevatorRequest extends Request {
    private Passenger elevatorRequest;

    public ElevatorRequest(Passenger elevatorRequest) {
        this.elevatorRequest = elevatorRequest;
    }

    public Passenger getElevatorRequest() {
        return elevatorRequest;
    }
}
