package team30.personalbest.walk;

import java.util.Date;

import team30.personalbest.fitness.FitnessSnapshot;
import team30.personalbest.fitness.StepType;

/**
 * Stores all static walk/run stats for the day.
 */
public class WalkStats implements FitnessSnapshot
{
    private final Date date;
    private final int intentionalSteps;
    private final int incidentalSteps;
    private final double distance;
    private final double mph;

    public WalkStats(Date date,
                     int intentionalSteps,
                     int incidentalSteps,
                     double distance,
                     double mph)
    {
        this.date = date;
        this.intentionalSteps = intentionalSteps;
        this.incidentalSteps = incidentalSteps;
        this.distance = distance;
        this.mph = mph;
    }

    @Override
    public double getDistanceTravelled()
    {
        return this.distance;
    }

    @Override
    public double getMilesPerHour()
    {
        return this.mph;
    }

    @Override
    public int getTotalSteps(StepType stepType)
    {
        switch(stepType)
        {
            case INTENTIONAL:
                return this.intentionalSteps;
            case INCIDENTAL:
                return this.incidentalSteps;
            case BOTH:
                return this.intentionalSteps + this.incidentalSteps;
        }

        throw new UnsupportedOperationException("Invalid step type");
    }

    @Override
    public Date getDate()
    {
        return this.date;
    }
}
