package team30.personalbest.fitness.snapshot;

public class FitnessSnapshot implements IFitnessSnapshot
{
    private long startTime;
    private long stopTime;
    private double speed;
    private int totalSteps;

    public FitnessSnapshot setTotalSteps(int steps)
    {
        this.totalSteps = steps;
        return this;
    }

    public FitnessSnapshot setStartTime(long startTime)
    {
        this.startTime = startTime;
        return this;
    }

    public FitnessSnapshot setStopTime(long stopTime)
    {
        this.stopTime = stopTime;
        return this;
    }

    public FitnessSnapshot setSpeed(double speed)
    {
        this.speed = speed;
        return this;
    }

    @Override
    public int getTotalSteps()
    {
        return this.totalSteps;
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

    @Override
    public double getSpeed()
    {
        return this.speed;
    }
}
