package team30.personalbest.fitness.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import team30.personalbest.fitness.Callback;
import team30.personalbest.fitness.snapshot.RecordingSnapshot;
import team30.personalbest.fitness.snapshot.FitnessSnapshot;
import team30.personalbest.fitness.snapshot.IFitnessSnapshot;

public class GoogleFitAdapter implements IFitnessService, IRecorderService
{
    public static final String TAG = "GoogleFitAdapter";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;

    public static final String RECORDING_SESSION_ID = "PersonalBestRun";
    public static final String RECORDING_SESSION_NAME = "Personal Best Run";
    public static final String RECORDING_SESSION_DESCRIPTION = "Doing a run";
    public static final int RECORDER_SAMPLING_RATE = 2;

    private final Activity activity;
    private final List<OnFitnessServiceReadyListener> fitnessListeners = new ArrayList<>();
    private final List<OnRecorderServiceReadyListener> recorderListeners = new ArrayList<>();

    private long customTime = -1;
    private boolean serviceReady = false;

    private boolean recording = false;
    private GoogleFitDataRecorder stepRecorder;
    private GoogleFitDataRecorder distanceRecorder;
    private RecordingSnapshot recordingSnapshot;
    private OnDataPointListener listener;

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
    public GoogleFitAdapter addOnFitnessServiceReady(OnFitnessServiceReadyListener listener)
    {
        this.fitnessListeners.add(listener);
        if (this.serviceReady)
        {
            listener.onFitnessServiceReady(this);
        }
        return this;
    }

    @Override
    public void removeOnFitnessServiceReady(OnFitnessServiceReadyListener listener)
    {
        this.fitnessListeners.remove(listener);
    }

    @Override
    public GoogleFitAdapter addOnRecorderServiceReady(OnRecorderServiceReadyListener listener)
    {
        this.recorderListeners.add(listener);
        if (this.serviceReady)
        {
            listener.onRecorderServiceReady(this);
        }
        return this;
    }

    @Override
    public void removeOnRecorderServiceReady(OnRecorderServiceReadyListener listener)
    {
        this.recorderListeners.remove(listener);
    }

    @Override
    public void onActivityCreate(Activity activity, Bundle savedInstanceState)
    {
        final FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_DISTANCE_DELTA)
                .addDataType(DataType.AGGREGATE_DISTANCE_DELTA)
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

        //Record steps passively...
        Fitness.getRecordingClient(this.activity, lastSignedInAccount)
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully subscribed google recording services for passive counting.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Failed to subscribe to google recording services for passive counting!", e);
                    }
                });

        this.serviceReady = true;

        for(OnFitnessServiceReadyListener listener : this.fitnessListeners)
        {
            listener.onFitnessServiceReady(this);
        }

        for(OnRecorderServiceReadyListener listener : this.recorderListeners)
        {
            listener.onRecorderServiceReady(this);
        }
    }

    @Override
    public RecordingSnapshot startRecording()
    {
        if (this.recording) throw new IllegalStateException("Already started recording");
        this.recording = true;

        this.recordingSnapshot = new RecordingSnapshot(this);

        this.stepRecorder = new GoogleFitDataRecorder(this.activity,
                DataType.TYPE_STEP_COUNT_CUMULATIVE,
                1).setHandler(this.recordingSnapshot);
        this.stepRecorder.start();

        /*
        this.stepRecorder = new GoogleFitDataRecorder(this.activity,
                DataType.TYPE_STEP_COUNT_CUMULATIVE,
                4).setHandler(this.recordingSnapshot);
        //this.distanceRecorder = new GoogleFitDataRecorder(this.activity, DataType.TYPE_DISTANCE_CUMULATIVE, 2, this.recordingSnapshot);

        this.stepRecorder.start();
        //this.distanceRecorder.start();
        */
        return this.recordingSnapshot;

    }

    @Override
    public Callback<IFitnessSnapshot> stopRecording()
    {
        if (!this.recording) throw new IllegalStateException("Must call startRecording first");
        this.recording = false;

        final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this.activity);
        if (lastSignedInAccount != null)
        {
            DataSet stepData = this.stepRecorder.stop();
            //DataSet distanceData = this.distanceRecorder.stop();

            if (stepData != null && !stepData.isEmpty())
            {
                final Session session = new Session.Builder()
                        .setName(RECORDING_SESSION_NAME)
                        .setDescription(RECORDING_SESSION_DESCRIPTION)
                        .setIdentifier(RECORDING_SESSION_ID)
                        .setActivity(FitnessActivities.RUNNING)
                        .setStartTime(this.recordingSnapshot.getStartTime(), TimeUnit.MILLISECONDS)
                        .setEndTime(this.recordingSnapshot.getStopTime(), TimeUnit.MILLISECONDS)
                        .build();
                final SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                        .setSession(session)
                        .addDataSet(stepData)
                        //.addDataSet(distanceData)
                        .build();

                Fitness.getSessionsClient(this.activity, lastSignedInAccount)
                        .insertSession(insertRequest)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(TAG, "Successfully inserted session data.");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Failed to insert session data.", e);
                            }
                        });
            }
            else
            {
                Log.w(TAG, "Found no fitness data to insert.");
            }
        }

        final RecordingSnapshot result = this.recordingSnapshot;
        this.recordingSnapshot = null;
        return new Callback<>(result);
    }

    @Override
    public boolean isRecording()
    {
        return this.recording;
    }

    @Override
    public RecordingSnapshot getRecordingSnapshot()
    {
        if (!this.recording) throw new IllegalStateException("Cannot get inactive recording snapshot");
        return this.recordingSnapshot;
    }

    @Override
    public Callback<Iterable<IFitnessSnapshot>> getRecordingSnapshots(long startTime, long stopTime)
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
                Log.d(TAG, "Getting multiple recording data between " + startTime +
                        " to " + stopTime + ", which is (" + (stopTime - startTime) + ")");

                SessionReadRequest readRequest = new SessionReadRequest.Builder()
                        .setTimeInterval(startTime, stopTime, TimeUnit.MILLISECONDS)
                        .read(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                        .read(DataType.TYPE_DISTANCE_CUMULATIVE)
                        .setSessionName(RECORDING_SESSION_NAME)
                        .build();

                Fitness.getSessionsClient(this.activity, lastSignedInAccount)
                        .readSession(readRequest)
                        .addOnSuccessListener(new OnSuccessListener<SessionReadResponse>() {
                            @Override
                            public void onSuccess(SessionReadResponse sessionReadResponse) {
                                List<IFitnessSnapshot> snapshots = new ArrayList<>();

                                final List<Session> sessions = sessionReadResponse.getSessions();
                                for(Session session : sessions)
                                {
                                    final List<DataSet> dataSets = sessionReadResponse.getDataSet(session);
                                    for(DataSet dataSet : dataSets)
                                    {
                                        for(DataPoint data : dataSet.getDataPoints())
                                        {
                                            FitnessSnapshot snapshot = new FitnessSnapshot();

                                            snapshot.setStartTime(data.getStartTime(TimeUnit.MILLISECONDS));
                                            snapshot.setStopTime(data.getEndTime(TimeUnit.MILLISECONDS));

                                            snapshot.setTotalSteps(
                                                    data.getValue(Field.FIELD_STEPS).asInt());
                                            snapshot.setSpeed(
                                                    data.getValue(Field.FIELD_DISTANCE).asFloat());

                                            snapshots.add(snapshot);
                                        }
                                    }
                                }

                                callback.resolve(snapshots);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Unable to get step count for duration", e);

                                callback.resolve(null);
                            }
                        });
            }
        }
        return callback;
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
                Log.d(TAG, "Getting multiple fitness data between " + startTime +
                        " to " + stopTime + ", which is (" + (stopTime - startTime) + ")");
                final DataReadRequest readRequest = new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .aggregate(DataType.TYPE_SPEED, DataType.AGGREGATE_SPEED_SUMMARY)
                        .bucketByTime(1, TimeUnit.DAYS)
                        .setTimeRange(startTime, stopTime + 1, TimeUnit.MILLISECONDS)
                        .setLimit(1)
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
                                        Log.d(TAG, "... ..." + data.toString());
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
