package team30.personalbest.goal;

import team30.personalbest.fitness.service.IFitnessService;

public class CustomStepGoal implements StepGoal
{
    private static final int DEFAULT_GOAL = 5000;

    private final IFitnessService fitnessService;
    private int goalValue;

    public CustomStepGoal(IFitnessService fitnessService)
    {
        this(fitnessService, DEFAULT_GOAL);
    }

    public CustomStepGoal(IFitnessService fitnessService, int initialGoal)
    {
        this.fitnessService = fitnessService;
        this.goalValue = initialGoal;

        //TODO: Implementation here.
        //Initialize here (i.e. Load from SharedPrefs)
    }

    public void setGoalValue(int value)
    {
        this.goalValue = value;

        //TODO: Implementation here.
        //Save to SharedPrefs here.
    }

    @Override
    public int getGoalValue()
    {
        return this.goalValue;
    }
}
