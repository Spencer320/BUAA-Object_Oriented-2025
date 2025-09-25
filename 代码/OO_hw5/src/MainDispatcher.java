import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;

import java.util.HashMap;

public class MainDispatcher extends Thread {
    private RequestTable requestTable;
    private HashMap<Integer, PassengerSet> waitingPassengerMap; // int: id of elevator

    public MainDispatcher(RequestTable requestTable,
        HashMap<Integer, PassengerSet> waitingPassengerMap) {
        this.requestTable = requestTable;
        this.waitingPassengerMap = waitingPassengerMap;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (requestTable) {
                if (requestTable.isEmpty() && requestTable.isEnd()) {
                    // System.out.println("All requests have been dispatched!!!");
                    for (PassengerSet waitingPassenger : waitingPassengerMap.values()) {
                        waitingPassenger.setEnd();
                    }
                    break;
                }
                Request request = requestTable.poll();
                if (request == null) {
                    continue;
                }
                dispatch(request);
            }
        }
    }

    private void dispatch(Request request) {
        if (request instanceof PersonRequest) {
            PersonRequest personRequest = (PersonRequest) request;
            Passenger passenger = new Passenger(personRequest.getPersonId(),
                Elevator.floorToNumber(personRequest.getFromFloor()),
                Elevator.floorToNumber(personRequest.getToFloor()),
                personRequest.getPriority());
            // System.out.println("A request has been dispatched.");
            waitingPassengerMap.get(personRequest.getElevatorId()).add(passenger);
        } else {
            throw new IllegalArgumentException("Invalid request");
        }
    }
}
