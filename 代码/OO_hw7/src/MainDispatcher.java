import com.oocourse.elevator3.TimableOutput;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.ScheRequest;
import com.oocourse.elevator3.UpdateRequest;

import java.util.HashMap;

public class MainDispatcher extends Thread {
    private final RequestTable requestTable;
    private final HashMap<Integer, PassengerSet> waitingPassengerMap; // int: id of elevator
    private final DispatchStrategy strategy;

    public MainDispatcher(RequestTable requestTable,
        HashMap<Integer, PassengerSet> waitingPassengerMap, DispatchStrategy strategy) {
        this.requestTable = requestTable;
        this.waitingPassengerMap = waitingPassengerMap;
        this.strategy = strategy;
    }

    @Override
    public void run() {
        while (true) {
            checkElevatorResendEnd();
            if (requestTable.isEmpty() && requestTable.isEnd()) {
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

    private void dispatch(Request request) {
        if (request instanceof ScheRequest) {
            dispatchSche((ScheRequest) request);
        } else if (request instanceof UpdateRequest) {
            dispatchUpdate((UpdateRequest) request);
        } else {
            while (strategy.getDelay() == DispatchStrategy.Delay.WAIT) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            switch (strategy.getDelay()) {
                case IGNORE:
                    handleIgnore(request);
                    break;
                case LOW:
                    handleLow(request);
                    break;
                case HIGH:
                    handleHigh(request);
                    break;
                default:
            }
        }
    }

    private void checkElevatorResendEnd() {
        boolean isSetEnd = requestTable.isEmpty() && requestTable.isInputEnd();
        // check resend from scheduling and updating
        isSetEnd &= strategy.checkElevatorRunnable();
        // check resend from double-car elevator
        isSetEnd &= strategy.checkFloorInRange();
        if (isSetEnd) {
            requestTable.setResendEnd();
        }
    }

    private void dispatchUpdate(UpdateRequest request) {
        int elevatorAId = request.getElevatorAId();
        int elevatorBId = request.getElevatorBId();
        int transferFloor = ElevatorTools.floorToNumber(request.getTransferFloor());
        strategy.setUpdate(elevatorAId, elevatorBId, transferFloor);
    }

    private void dispatchSche(ScheRequest request) {
        int elevatorId = request.getElevatorId();
        int scheSpeed = (int) (request.getSpeed() * 1000); // millis
        int scheFloor = ElevatorTools.floorToNumber(request.getToFloor());
        waitingPassengerMap.get(elevatorId).setSche(true, scheSpeed, scheFloor);
    }

    private void dispatchNonSche(ElevatorRequest request) {
        Passenger passenger = new Passenger(request.getElevatorRequest());
        int elevatorId = strategy.getElevatorId(passenger);
        ElevatorTools.formatOutput(ElevatorTools.OutputType.RECEIVE, passenger, elevatorId);
        waitingPassengerMap.get(elevatorId).add(passenger);
    }

    private void dispatchNonSche(PersonRequest request) {
        Passenger passenger = new Passenger(request.getPersonId(),
            ElevatorTools.floorToNumber(request.getFromFloor()),
            ElevatorTools.floorToNumber(request.getToFloor()),
            request.getPriority());
        int elevatorId = strategy.getElevatorId(passenger);
        TimableOutput.println("RECEIVE-" + passenger.getId() + "-" + elevatorId);
        waitingPassengerMap.get(elevatorId).add(passenger);
    }

    private void dispatchNonSche(Request request) {
        if (request instanceof ElevatorRequest) {
            dispatchNonSche((ElevatorRequest) request);
        } else if (request instanceof PersonRequest) {
            dispatchNonSche((PersonRequest) request);
        } else {
            throw new IllegalArgumentException("Invalid request");
        }
    }

    private void handleIgnore(Request request) {
        try {
            sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        dispatchNonSche(request);
    }

    private void handleLow(Request request) {
        try {
            sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        dispatchNonSche(request);
    }

    private void handleHigh(Request request) {
        int times = 5;
        for (int i = 0; i < times; i++) {
            try {
                sleep(400);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (! requestTable.isScheEmpty()) {
                // get schedule request while long wait
                ScheRequest scheRequest = (ScheRequest) requestTable.poll();
                dispatchSche(scheRequest);
            }
        }
        dispatchNonSche(request);
    }

}
