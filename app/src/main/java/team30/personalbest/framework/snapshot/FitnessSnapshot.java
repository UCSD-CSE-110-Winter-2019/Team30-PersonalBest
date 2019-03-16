package team30.personalbest.framework.snapshot;

public class FitnessSnapshot implements IFitnessSnapshot {
    private long startTime;
    private long stopTime;
    private double speed;
    private int totalSteps;
    private int recordedSteps;

    @Override
    public int getRecordedSteps() {
        return this.recordedSteps;
    }

    public FitnessSnapshot setRecordedSteps(int steps) {
        this.recordedSteps = steps;
        return this;
    }

    @Override
    public int getTotalSteps() {
        return this.totalSteps;
    }

    public FitnessSnapshot setTotalSteps(int steps) {
        this.totalSteps = steps;
        return this;
    }

    @Override
    public long getStartTime() {
        return this.startTime;
    }

    public FitnessSnapshot setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    @Override
    public long getStopTime() {
        return this.stopTime;
    }

    public FitnessSnapshot setStopTime(long stopTime) {
        this.stopTime = stopTime;
        return this;
    }

    @Override
    public double getSpeed() {
        return this.speed;
    }

    public FitnessSnapshot setSpeed(double speed) {
        this.speed = speed;
        return this;
    }
}
