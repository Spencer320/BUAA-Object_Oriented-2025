public class DoubleCarElevatorManager extends Thread {
    private final Elevator elevatorA;
    private final Elevator elevatorB;
    private final int transferFloor;
    private boolean occupied = false;

    public DoubleCarElevatorManager(Elevator elevatorA, Elevator elevatorB, int transferFloor) {
        this.elevatorA = elevatorA;
        this.elevatorB = elevatorB;
        this.transferFloor = transferFloor;
    }

    @Override
    public void run() {
        setP();
        do {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (! beginCheck());
        ElevatorTools.formatOutput(ElevatorTools.OutputType.BEGIN_U,
            elevatorA.getElevatorId(), elevatorB.getElevatorId());
        setI();
        try {
            sleep(Elevator.Constant.StopTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        setD();
        ElevatorTools.formatOutput(ElevatorTools.OutputType.END_U,
            elevatorA.getElevatorId(), elevatorB.getElevatorId());
    }

    private void setP() {
        elevatorA.setUpdateP(Elevator.ElevatorType.DA, transferFloor);
        elevatorB.setUpdateP(Elevator.ElevatorType.DB, transferFloor);
    }

    private void setI() {
        elevatorA.setUpdateI();
        elevatorB.setUpdateI();
    }

    private void setD() {
        elevatorA.setUpdateD(this);
        elevatorB.setUpdateD(this);
    }

    private boolean beginCheck() {
        return elevatorA.isReady() && elevatorB.isReady();
    }

    public synchronized void get() {
        while (occupied) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        occupied = true;
        notifyAll();
    }

    public synchronized void release() {
        occupied = false;
        notifyAll();
    }
}