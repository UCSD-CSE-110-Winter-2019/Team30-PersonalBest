package team30.personalbest.framework.snapshot;

public interface IFitnessSnapshot {
    int getRecordedSteps();

    int getTotalSteps();

    long getStartTime();

    long getStopTime();

    double getSpeed();
}
