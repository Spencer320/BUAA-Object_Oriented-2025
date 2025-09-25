import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.ScheRequest;
import com.oocourse.elevator2.TimableOutput;

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
            checkElevatorScheEnd();
            if (requestTable.isEmpty() && requestTable.isEnd()) {
                for (PassengerSet waitingPassenger : waitingPassengerMap.values()) {
                    waitingPassenger.setEnd();
                }
                // System.out.println("MainDispatcher has ended!");
                break;
            }
            Request request = requestTable.poll();
            if (request == null) {
                continue;
            }
            // System.out.println("A request dispatched");
            dispatch(request);
        }
    }

    private void dispatch(Request request) {
        if (request instanceof ScheRequest) {
            dispatchSche((ScheRequest) request);
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

    private void checkElevatorScheEnd() {
        boolean isSetEnd = requestTable.isEmpty() && requestTable.isInputEnd();
        for (PassengerSet set : waitingPassengerMap.values()) {
            if (set.isSche()) {
                isSetEnd = false;
                break;
            }
        }
        if (isSetEnd) {
            requestTable.setResendEnd();
        }
    }

    private void dispatchSche(ScheRequest request) {
        int elevatorId = request.getElevatorId();
        int scheSpeed = (int) (request.getSpeed() * 1000); // millis
        int scheFloor = Elevator.floorToNumber(request.getToFloor());
        waitingPassengerMap.get(elevatorId).setSche(true, scheSpeed, scheFloor);
    }

    private void dispatchNonSche(ElevatorRequest request) {
        Passenger passenger = new Passenger(request.getElevatorRequest());
        int elevatorId = strategy.getElevatorId(passenger);
        TimableOutput.println("RECEIVE-" + passenger.getId() + "-" + elevatorId);
        waitingPassengerMap.get(elevatorId).add(passenger);
    }

    private void dispatchNonSche(PersonRequest request) {
        Passenger passenger = new Passenger(request.getPersonId(),
            Elevator.floorToNumber(request.getFromFloor()),
            Elevator.floorToNumber(request.getToFloor()),
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



