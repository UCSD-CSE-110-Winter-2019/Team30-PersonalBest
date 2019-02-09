package team30.personalbest.fitness;

import java.util.Date;

public interface FitnessSnapshot
{
    double getDistanceTravelled();
    double getMilesPerHour();
    int getTotalSteps(StepType stepType);
    Date getDate();
}
