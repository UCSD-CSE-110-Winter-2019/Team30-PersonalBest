package team30.personalbest.fitness.snapshot;

public interface IFitnessSnapshot
{
    int getTotalSteps();
    double getDistanceTravelled();
    double getMilesPerHour();
    long getStartTime();
    long getStopTime();
}
