package team30.personalbest.fitness.service;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import team30.personalbest.fitness.Callback;
import team30.personalbest.fitness.snapshot.FitnessSnapshot;
import team30.personalbest.fitness.snapshot.IFitnessSnapshot;

public class GoogleFitAdapter implements IFitnessService
{
    public static final String TAG = "GoogleFitAdapter";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;

    private final Activity activity;
    private final List<OnFitnessServiceReadyListener> listeners = new ArrayList<>();

    private long customTime = -1;
    private boolean serviceReady = false;
    private boolean recording = false;

    public GoogleFitAdapter(Activity activity)
    {
        this.activity = activity;
    }

    public GoogleFitAdapter setCurrentTime(long millis)
    {
        this.customTime = millis;
        return this;
    }

    @Override
    public void startRecording()
    {
        if (this.recording) throw new IllegalStateException("Already started recording");
        this.recording = true;

        final long currentTime = this.getCurrentTime();
        SharedPreferences sharedPreferences = null;
                //.putLong("startwalk", currentTime).apply();
    }

    @Override
    public Callback<IFitnessSnapshot> stopRecording()
    {
        if (!this.recording) throw new IllegalStateException("Must call startRecording first");
        this.recording = false;

        final Callback<IFitnessSnapshot> callback = this.getFitnessSnapshot();//new Callback<>();

        return callback;
    }

    @Override
    public boolean isRecording()
    {
        return this.recording;
    }

    @Override
    public GoogleFitAdapter addOnFitnessServiceReady(OnFitnessServiceReadyListener listener)
    {
        this.listeners.add(listener);
        if (this.serviceReady)
        {
            listener.onFitnessServiceReady(this);
        }
        return this;
    }

    @Override
    public void onActivityCreate(Activity activity, Bundle savedInstanceState)
    {
        final FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .addDataType(DataType.TYPE_SPEED)
                .addDataType(DataType.AGGREGATE_SPEED_SUMMARY)
                .addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_WRITE)
                .build();

        if (!GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(this.activity),
                fitnessOptions))
        {
            GoogleSignIn.requestPermissions(
                    this.activity,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this.activity),
                    fitnessOptions
            );
        }
        else
        {
            this.subscribeToGoogleServices(activity);
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, @Nullable Intent data)
    {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_OAUTH_REQUEST_CODE)
        {
            this.subscribeToGoogleServices(activity);
        }
    }

    private void subscribeToGoogleServices(Activity activity)
    {
        final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity);
        if (lastSignedInAccount == null) return;

        Fitness.getRecordingClient(this.activity, lastSignedInAccount)
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully subscribed google recording services.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Failed to subscribe to google recording services!", e);
                    }
                });

        this.serviceReady = true;

        for(OnFitnessServiceReadyListener listener : this.listeners)
        {
            listener.onFitnessServiceReady(this);
        }
    }

    @Override
    public Callback<Iterable<IFitnessSnapshot>> getFitnessSnapshots(long startTime, long stopTime)
    {
        final Callback<Iterable<IFitnessSnapshot>> callback = new Callback<>();
        {
            final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this.activity);
            if (lastSignedInAccount == null)
            {
                callback.resolve(null);
            }
            else
            {
                final DataReadRequest readRequest = new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .aggregate(DataType.TYPE_SPEED, DataType.AGGREGATE_SPEED_SUMMARY)
                        .bucketByTime(1, TimeUnit.DAYS)
                        .setTimeRange(startTime, stopTime, TimeUnit.MILLISECONDS)
                        .build();
                Fitness.getHistoryClient(this.activity, lastSignedInAccount)
                        .readData(readRequest)
                        .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                Log.d(TAG, "Retrieving multiple fitness data...");
                                final List<IFitnessSnapshot> result = new ArrayList<>();
                                final List<DataSet> dataSets = dataReadResponse.getDataSets();
                                for(DataSet dataSet : dataSets)
                                {
                                    Log.d(TAG, "..." + dataSet.toString());
                                    for(DataPoint data : dataSet.getDataPoints())
                                    {
                                        final FitnessSnapshot snapshot = new FitnessSnapshot()
                                                .setTotalSteps(data.getValue(Field.FIELD_STEPS).asInt())
                                                .setStartTime(data.getStartTime(TimeUnit.MILLISECONDS))
                                                .setStopTime(data.getEndTime(TimeUnit.MILLISECONDS))
                                                .setSpeed(data.getValue(Field.FIELD_SPEED).asFloat());
                                        result.add(snapshot);
                                    }
                                }
                                callback.resolve(result);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Failed to retrieve fitness data for range.", e);
                            }
                        });
            }
        }
        return callback;
    }

    @Override
    public Callback<IFitnessSnapshot> getFitnessSnapshot()
    {
        final Callback<IFitnessSnapshot> callback = new Callback<>();
        {
            final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this.activity);
            if (lastSignedInAccount == null)
            {
                callback.resolve(null);
            }
            else
            {
                Fitness.getHistoryClient(this.activity, lastSignedInAccount)
                        .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                        .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                Log.d(TAG, "Retrieving fitness data..." + dataSet.toString());

                                FitnessSnapshot result = null;
                                if (!dataSet.isEmpty())
                                {
                                    final DataPoint dataPoint = dataSet.getDataPoints().get(0);
                                    result = new FitnessSnapshot()
                                            .setTotalSteps(dataPoint.getValue(Field.FIELD_STEPS).asInt())
                                            .setStartTime(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                                            .setStopTime(dataPoint.getEndTime(TimeUnit.MILLISECONDS));
                                            //.setSpeed(dataPoint.getValue(Field.FIELD_SPEED).asFloat());
                                }

                                callback.resolve(result);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Failed to retrieve fitness data.", e);
                            }
                        });
            }
                /*
                final long stopTime = this.getCurrentTime();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(stopTime);
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                final long startTime = calendar.getTimeInMillis();

                final DataReadRequest readRequest = new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .aggregate(DataType.TYPE_SPEED, DataType.AGGREGATE_SPEED_SUMMARY)
                        .bucketByTime(1, TimeUnit.DAYS)
                        .setTimeRange(startTime, stopTime, TimeUnit.MILLISECONDS)
                        .build();
                Fitness.getHistoryClient(this.activity, lastSignedInAccount)
                        .readData(readRequest)
                        .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                if (dataReadResponse.getDataSets().isEmpty())
                                {
                                    Log.w(TAG, "Could not find recent fitness data");
                                    callback.resolve(null);
                                }
                                else
                                {
                                    DataSet dataSet = dataReadResponse.getDataSets().get(0);
                                    Log.d(TAG, "Retrieving fitness data..." + dataSet.toString());

                                    FitnessSnapshot result = null;
                                    if (!dataSet.isEmpty())
                                    {
                                        final DataPoint dataPoint = dataSet.getDataPoints().get(0);
                                        result = new FitnessSnapshot()
                                                .setTotalSteps(dataPoint.getValue(Field.FIELD_STEPS).asInt())
                                                .setStartTime(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                                                .setStopTime(dataPoint.getEndTime(TimeUnit.MILLISECONDS))
                                                .setSpeed(dataPoint.getValue(Field.FIELD_SPEED).asFloat());
                                    }

                                    callback.resolve(result);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Failed to retrieve fitness data.", e);
                            }
                        });
            }
            */
        }
        return callback;
    }

    @Override
    public long getCurrentTime()
    {
        if (this.customTime >= 0)
        {
            return this.customTime;
        }
        else
        {
            return System.currentTimeMillis();
        }
    }

    @Override
    public boolean isServiceReady()
    {
        return this.serviceReady;
    }

    public Activity getActivity()
    {
        return this.activity;
    }
}
