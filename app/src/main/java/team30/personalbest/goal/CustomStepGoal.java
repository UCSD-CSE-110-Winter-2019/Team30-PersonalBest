package team30.personalbest.goal;

import com.google.android.gms.fitness.data.Goal;

import java.util.List;

import team30.personalbest.fitness.service.IFitnessService;

public class CustomStepGoal implements StepGoal
{
    private final String LOG_TAG = "PersonalBest";
    private static final int DEFAULT_GOAL = 5000;
    private Goal.MetricObjective stepGoal;
    private List<Goal> goals;

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
        stepGoal = new Goal.MetricObjective( "Daily Steps", 0, initialGoal );


        //TODO: Implementation here.
        //Initialize here (i.e. Load from SharedPrefs)
    }



    public void setGoalValue(int value)
    {
        this.goalValue = value;

        //TODO (Chen) : Make Custom DataType for Goals
        //Save to SharedPrefs here.
    }

    @Override
    public int getGoalValue()
    {
        return this.goalValue;
    }
}
