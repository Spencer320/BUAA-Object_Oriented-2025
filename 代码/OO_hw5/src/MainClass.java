import com.oocourse.elevator1.TimableOutput;

import java.util.HashMap;

public class MainClass {
    private static int elevatorNum = 6;

    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();

        RequestTable mainRequestTable = new RequestTable();
        InputThread inputThread = new InputThread(mainRequestTable);
        HashMap<Integer, PassengerSet> waitingPassengerMap = new HashMap<>();
        MainDispatcher dispatcher = new MainDispatcher(mainRequestTable, waitingPassengerMap);
        for (int id = 1; id <= elevatorNum; id++) {
            // create an elevator
            PassengerSet waitingPassenger = new PassengerSet();
            waitingPassengerMap.put(id, waitingPassenger);
            Elevator elevator = new Elevator(id,waitingPassenger);
            elevator.start();
        }
        inputThread.start();
        dispatcher.start();
    }

}

