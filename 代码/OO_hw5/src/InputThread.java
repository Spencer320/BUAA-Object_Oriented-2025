import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.Request;

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
                requestTable.setEnd();
                break;
            } else {
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

