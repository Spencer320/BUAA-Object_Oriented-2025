import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.Request;

import java.io.IOException;

public class InputThread extends Thread {
    private final RequestTable requestTable;

    public InputThread(RequestTable requestTable) {
        this.requestTable = requestTable;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                requestTable.setInputEnd();
                // System.out.println("InputThread has ended!!!");
                break;
            } else {
                // System.out.println("Request from input.");
                requestTable.offer(request);
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

