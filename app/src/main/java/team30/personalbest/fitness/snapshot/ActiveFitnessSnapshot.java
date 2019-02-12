package team30.personalbest.fitness.snapshot;

public class ActiveFitnessSnapshot implements IFitnessSnapshot
{
    ActiveFitnessSnapshot(long startTime)
    {

    }

    @Override
    public double getDistanceTravelled()
    {
        return 0;
    }

    @Override
    public double getMilesPerHour() {
        return 0;
    }

    @Override
    public int getTotalSteps() {
        return 0;
    }

    @Override
    public long getStartTime() {
        return 0;
    }

    @Override
    public long getStopTime() {
        return 0;
    }
}
