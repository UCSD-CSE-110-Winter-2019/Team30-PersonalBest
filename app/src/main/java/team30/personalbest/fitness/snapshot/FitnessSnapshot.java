package team30.personalbest.fitness.snapshot;

public class FitnessSnapshot implements IFitnessSnapshot
{
    private long startTime;
    private long stopTime;
    private double distance;
    private int totalSteps;

    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    }

    public void setStopTime(long stopTime)
    {
        this.stopTime = stopTime;
    }

    public void setDistanceTravelled(double distance)
    {
        this.distance = distance;
    }

    public void setTotalSteps(int steps)
    {
        this.totalSteps = steps;
    }

    @Override
    public int getTotalSteps()
    {
        return this.totalSteps;
    }

    @Override
    public double getDistanceTravelled()
    {
        return this.distance;
    }

    @Override
    public double getMilesPerHour()
    {
        return this.distance / (this.stopTime - this.startTime);
    }

    @Override
    public long getStartTime()
    {
        return this.startTime;
    }

    @Override
    public long getStopTime()
    {
        return this.stopTime;
    }
}
