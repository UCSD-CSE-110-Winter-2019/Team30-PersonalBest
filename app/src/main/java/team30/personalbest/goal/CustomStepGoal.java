package team30.personalbest.goal;

import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Goal;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.GoalsReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import team30.personalbest.fitness.GoogleFitAdapter;
import team30.personalbest.fitness.service.IFitnessService;
import team30.personalbest.fitness.snapshot.IFitnessSnapshot;

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
