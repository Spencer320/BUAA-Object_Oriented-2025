import com.oocourse.elevator3.TimableOutput;

import java.util.ArrayList;
import java.util.HashMap;

public class MainClass {
    private static int elevatorNum = 6;

    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();

        RequestTable requestTable = new RequestTable();
        InputThread inputThread = new InputThread(requestTable);
        HashMap<Integer, PassengerSet> waitingPassengerMap = new HashMap<>();
        ArrayList<Elevator> elevators = new ArrayList<>();
        DispatchStrategy strategy = new DispatchStrategy(elevators);
        MainDispatcher dispatcher = new MainDispatcher(requestTable, waitingPassengerMap, strategy);
        for (int id = 1; id <= elevatorNum; id++) {
            // create an elevator
            PassengerSet waitingPassenger = new PassengerSet();
            waitingPassengerMap.put(id, waitingPassenger);
            Elevator elevator = new Elevator(id, waitingPassenger,requestTable);
            elevators.add(elevator);
            elevator.start();
        }
        inputThread.start();
        dispatcher.start();
    }

}

