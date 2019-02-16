package team30.personalbest.goal;

import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.ConfigApi;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Goal;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataTypeCreateRequest;
import com.google.android.gms.fitness.request.GoalsReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.DataTypeResult;
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

import static com.google.android.gms.fitness.ConfigApi.*;

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

        // 1. Build a request to create a new data type
        DataTypeCreateRequest request = new DataTypeCreateRequest.Builder()
                // The prefix of your data type name must match your app's package name
                .setName("team30.personalbest.goal")
                // Add some custom fields, both int and float
                .addField("goal", Field.FORMAT_INT32)
                // Add some common fields
                .addField(Field.FIELD_ACTIVITY)
                .build();

        // 2. Invoke the Config API with:
        // - The Google API client object
        // - The create data type request
        ConfigApi configApi = new ConfigApi() {
            @Override
            public PendingResult<DataTypeResult> createCustomDataType(GoogleApiClient googleApiClient, DataTypeCreateRequest dataTypeCreateRequest) {
                return null;
            }

            @Override
            public PendingResult<DataTypeResult> readDataType(GoogleApiClient googleApiClient, String s) {
                return null;
            }

            @Override
            public PendingResult<Status> disableFit(GoogleApiClient googleApiClient) {
                return null;
            }
        };

        PendingResult<DataTypeResult> pendingResult =
                configApi.createCustomDataType(null, request);

        // 3. Check the result asynchronously
        // (The result may not be immediately available)
        pendingResult.setResultCallback(
                new ResultCallback<DataTypeResult>() {
                    @Override
                    public void onResult(DataTypeResult dataTypeResult) {
                        // Retrieve the created data type
                        DataType customType = dataTypeResult.getDataType();
                        // Use this custom data type to insert data in your app
                        // (see other examples)
                    }
                }
        );

        //Save to SharedPrefs here.
    }

    @Override
    public int getGoalValue()
    {
        return this.goalValue;
    }
}
