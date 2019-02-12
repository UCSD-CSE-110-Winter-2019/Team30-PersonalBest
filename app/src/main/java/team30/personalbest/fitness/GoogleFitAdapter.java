package team30.personalbest.fitness;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Consumer;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class GoogleFitAdapter
{
    public static final String TAG = "GoogleFitAdapter";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;

    private Consumer<GoogleFitAdapter> onReadyListener;
    private Activity activity;

    public GoogleFitAdapter setOnReadyListener(Consumer<GoogleFitAdapter> listener)
    {
        this.onReadyListener = listener;
        return this;
    }

    public void onActivityCreate(Activity activity, Bundle savedInstanceState)
    {
        this.activity = activity;

        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .addDataType(DataType.TYPE_DISTANCE_CUMULATIVE)
                        .addDataType(DataType.TYPE_DISTANCE_DELTA)
                        .addDataType(DataType.TYPE_SPEED)
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
                        if (googleFitAdapter.onReadyListener != null)
                        {
                            googleFitAdapter.onReadyListener.accept(googleFitAdapter);
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

    public GoogleSignInAccount getCurrentGoogleAccount()
    {
        return GoogleSignIn.getLastSignedInAccount(this.activity);
    }

    public Activity getActivity()
    {
        return this.activity;
    }

    public long getCurrentTime()
    {
        return System.currentTimeMillis();
    }
}
