package team30.personalbest;



import android.annotation.TargetApi;


import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

public class GoogleFitLayer {
    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
    private MainActivity activity;

    private final String LOG_TAG = "PersonalBest";
    private int dailyStepCount = 0;

    private static float height;

    private DataSource userStats;
    private DataPoint userHeight;




    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    public GoogleFitLayer(MainActivity activity) {
        this.activity = activity;


//          userStats = new DataSource.Builder()
//                .setDataType( DataType.TYPE_HEIGHT)
//                .setDataType( DataType.TYPE_SPEED)
//                .build();
//         this.userHeight = DataPoint.create( userStats );



        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_DISTANCE_CUMULATIVE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_SPEED_SUMMARY, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_WRITE)
                .build();

        GoogleSignInAccount gsa = GoogleSignIn.getAccountForExtension(activity.getApplicationContext(), fitnessOptions);
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity.getApplicationContext()), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    activity, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(activity.getApplicationContext()),
                    fitnessOptions);
        } else {
            //accessGoogleFit();
            subscribe();
        }

        Log.i(LOG_TAG, "Height = " + height );

    }

    /* Starts recoding steps */
    public void subscribe() {
        Fitness.getRecordingClient( activity, GoogleSignIn.getLastSignedInAccount(activity))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if( task.isSuccessful()) {
                            Log.i(LOG_TAG, "Successfuly subscribed!");
                        } else {
                            Log.w( LOG_TAG, "There was a problem subscribing.", task.getException() );
                        }
                    }
                });
    }

    private void accessGoogleFit() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.YEAR, -1);
        long startTime = cal.getTimeInMillis();


        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();



        Fitness.getHistoryClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        Log.d(LOG_TAG, "onSuccess()");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "onFailure()", e);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<DataReadResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<DataReadResponse> task) {
                        Log.d(LOG_TAG, "onComplete()");
                    }
                });
    }

    /* Adapted from Google Fit Samples
     * https://github.com/googlesamples/android-fit/blob/master/StepCounter/app/src/main/java/com/google/android/gms/fit/samples/stepcounter/MainActivity.java
     */
    public void readDailyStepCount() {
        long total = 0;
        Fitness.getHistoryClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {

                        if( dataSet.isEmpty() ){ Log.i(LOG_TAG, "Dataset is empty"); }
                        long total =
                                dataSet.isEmpty()
                                        ? 0
                                        : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                        Log.i(LOG_TAG, "Total steps: " + total);

                        activity.setDailySteps( total );

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w( LOG_TAG, "There was a problem getting the step count.", e);
                    }
                });
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public int getDailyStepCount() {
        Task<DataSet> response = Fitness.getHistoryClient(activity, GoogleSignIn.getLastSignedInAccount(activity)).readDailyTotal(DataType.TYPE_STEP_COUNT_CUMULATIVE);

        int stepCount = 0;
        DataSet totalSet = null;
        try {
            totalSet = Tasks.await(response, 30, SECONDS);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        if( totalSet != null) {  stepCount = totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();}
        activity.setDailySteps( stepCount );
        return stepCount;
    }

    public static void setHeight( float height ) {
        GoogleFitLayer.height = height;
        System.out.println("Success");
    }

    public void startSession() {

    }

}
