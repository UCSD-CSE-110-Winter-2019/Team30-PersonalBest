package team30.personalbest.fitness;

public interface FitnessSnapshot
{
    double getDistanceTravelled();
    double getMilesPerHour();
    int getTotalSteps(StepType stepType);
    long getTime();
}
