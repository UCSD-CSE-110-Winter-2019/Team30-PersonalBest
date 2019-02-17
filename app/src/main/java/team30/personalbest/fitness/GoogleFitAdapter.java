package team30.personalbest.fitness;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class GoogleFitAdapter implements IGoogleFitAdapter
{
    public static final String TAG = "GoogleFitAdapter";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;

    private List<OnGoogleFitReadyListener> onReadyListeners = new ArrayList<>();
    private boolean ready = false;
    private Activity activity;

    public GoogleFitAdapter(Activity activity)
    {
        this.activity = activity;
    }

    @Override
    public GoogleFitAdapter addOnReadyListener(OnGoogleFitReadyListener listener)
    {
        this.onReadyListeners.add(listener);

        //Call it now, since it won't be called.
        if (this.ready)
        {
            listener.onGoogleFitReady(this);
        }

        return this;
    }

    @Override
    public void removeOnReadyListener(OnGoogleFitReadyListener listener)
    {
        this.onReadyListeners.remove(listener);
    }

    @Override
    public void onActivityCreate(Activity activity, Bundle savedInstanceState)
    {
        this.activity = activity;

        //Default access is ACCESS_READ
        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        //This is since the beginning of time.
                        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                        .addDataType(DataType.TYPE_DISTANCE_CUMULATIVE)
                        //This is since the duration of time.
                        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .addDataType(DataType.AGGREGATE_DISTANCE_DELTA)
                        .addDataType(DataType.AGGREGATE_SPEED_SUMMARY)
                        //This is any time.
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .addDataType(DataType.TYPE_DISTANCE_DELTA)
                        .addDataType(DataType.TYPE_SPEED)
                        .addDataType(DataType.TYPE_MOVE_MINUTES)
                        //I want to write height :P
                        .addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_WRITE)
                        .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this.activity), fitnessOptions))
        {
            GoogleSignIn.requestPermissions(
                    activity,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this.activity),
                    fitnessOptions);
        }
        else
        {
            this.subscribeGoogleServices(activity);
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, @Nullable Intent data)
    {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_OAUTH_REQUEST_CODE)
        {
            this.subscribeGoogleServices(activity);
        }
    }

    private void subscribeGoogleServices(Activity activity)
    {
        Log.i(TAG, "Subscribing to google services...");

        Fitness.getRecordingClient(activity, this.getCurrentGoogleAccount())
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        GoogleFitAdapter googleFitAdapter = GoogleFitAdapter.this;
                        googleFitAdapter.ready = true;

                        for(OnGoogleFitReadyListener listener : googleFitAdapter.onReadyListeners)
                        {
                            try
                            {
                                listener.onGoogleFitReady(googleFitAdapter);
                            }
                            catch (Exception e)
                            {
                                Log.w(TAG, "Failed to handle google fit ready event", e);
                            }
                        }

                        Log.i(TAG, "Successfully subscribed google services");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Unable to initialize google services", e);
                    }
                });
    }

    @Override
    public GoogleSignInAccount getCurrentGoogleAccount()
    {
        return GoogleSignIn.getLastSignedInAccount(this.activity);
    }

    @Override
    public Activity getActivity()
    {
        return this.activity;
    }

    @Override
    public long getCurrentTime()
    {
        return System.currentTimeMillis();
    }

    @Override
    public boolean isReady()
    {
        return this.ready;
    }
}
