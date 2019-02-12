package team30.personalbest.walk.intentional;

import team30.personalbest.fitness.StepType;
import team30.personalbest.fitness.FitnessSnapshot;

public class IntentionalWalkStats implements FitnessSnapshot
{
    public IntentionalWalkStats()
    {
        //TODO: Implementation here.
    }

    public boolean isComplete()
    {
        //TODO: Implementation here.
        //Update this to reflect when the walk is done
        return false;
    }

    @Override
    public double getDistanceTravelled()
    {
        //TODO: Implementation here.
        return 0;
    }

    @Override
    public double getMilesPerHour()
    {
        //TODO: Implementation here.
        return 0;
    }

    @Override
    public int getTotalSteps(StepType stepType)
    {
        if (stepType != StepType.INTENTIONAL)
        {
            throw new IllegalStateException("Is currently in intentional steps");
        }

        //TODO: Implementation here.

        return 0;
    }

    @Override
    public long getTime()
    {
        //TODO: Implementation here.
        return null;
    }
}
