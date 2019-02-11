package team30.personalbest;

import android.annotation.TargetApi;
import android.icu.util.Calendar;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.Date;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

public class GoogleFitLayer {
    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
    private MainActivity activity;

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    public GoogleFitLayer(MainActivity activity) {
        this.activity = activity;

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
            accessGoogleFit();
        }
    }

    private void accessGoogleFit() {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public int getDailyStepCount() {
        Task<DataSet> response = Fitness.getHistoryClient(activity, GoogleSignIn.getLastSignedInAccount(activity)).readDailyTotal(DataType.TYPE_STEP_COUNT_CUMULATIVE);

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

        int stepCount = totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
        return stepCount;
    }

}
