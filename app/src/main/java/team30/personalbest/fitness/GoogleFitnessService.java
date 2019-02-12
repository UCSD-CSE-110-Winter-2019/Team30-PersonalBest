package team30.personalbest.fitness;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import team30.personalbest.walk.WalkStats;

public class GoogleFitnessService implements FitnessService
{
    public static final String TAG = "GoogleFitnessService";

    private static final int REQUEST_OAUTH_CODE = 0x1010;
    private final Activity activity;

    public GoogleFitnessService(Activity activity)
    {
        if (activity == null) throw new IllegalArgumentException("Must specify activity");

        this.activity = activity;

        //this.initializeGoogleFit();
    }

    /*
    private void initializeGoogleFit()
    {
        final FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                //.addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                //.addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                //.addDataType(DataType.TYPE_DISTANCE_CUMULATIVE, FitnessOptions.ACCESS_READ)
                //.addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_READ)
                //.addDataType(DataType.AGGREGATE_SPEED_SUMMARY, FitnessOptions.ACCESS_READ)
                //.addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                //.addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
                //.addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_WRITE)
                .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this.activity),
                fitnessOptions))
        {
            GoogleSignIn.requestPermissions(
                    this.activity,
                    REQUEST_OAUTH_CODE,
                    GoogleSignIn.getLastSignedInAccount(this.activity),
                    fitnessOptions);
        }
        else
        {
            this.onGoogleFitEnabled(this.activity);
        }
    }

    protected void onGoogleFitEnabled(Activity activity)
    {
        Fitness.getRecordingClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully subscribed...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Failed to subscribe!", e);
                    }
                });
    }

    //Never un-subscribed, but it's okay...
    protected void onGoogleFitDisabled(Activity activity)
    {
        Fitness.getRecordingClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                .unsubscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE);
    }

    public FitnessService getWeeklyFitnessService()
    {
        BuildableFitnessService result = new BuildableFitnessService();
        result.addSnapshot();
        return result;
    }

    */
    @Override
    public Callback<FitnessSnapshot> getFitnessSnapshot(long time)
    {
        //Prepare the async callback...
        final Callback<FitnessSnapshot> callback = new Callback<>();

        /*
        final GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(this.activity);
        Fitness.getHistoryClient(this.activity, gsa)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {
                        int result;
                        if (dataSet.isEmpty())
                        {
                            result = 0;
                        }
                        else
                        {
                            result = dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                        }

                        callback.resolve(null);
                    }
                });
        */

        //TODO: Get all data for the whole day of date (ignore time)
        //This can be called at any time.
        //Construct this.
        WalkStats result = null;

        //Make this call when your async call finishes.
        callback.resolve(result);

        //Return the async callback
        return callback;
    }

    @Override
    public Callback<Integer> getProgressSteps()
    {
        //Prepare the async callback...
        final Callback<Integer> callback = new Callback<>();

        //TODO: Get steps since last goal met.
        //We can either store the time at which a goal was met
        //Or store the total steps before a goal was met
        //Or something to calculate how much progress was made since last goal.

        //Compute this.
        int result = 0;

        //Make this call when your async call finishes.
        callback.resolve(result);

        //Return the async callback
        return callback;
    }
}
