package team30.personalbest.fitness.service;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import team30.personalbest.fitness.Callback;
import team30.personalbest.fitness.GoogleFitAdapter;
import team30.personalbest.fitness.snapshot.ActiveFitnessSnapshot;
import team30.personalbest.fitness.snapshot.FitnessSnapshot;
import team30.personalbest.fitness.snapshot.IFitnessSnapshot;

public class ActiveFitnessService implements IFitnessService
{
    public static final String TAG = "ActiveFitnessService";
    public static final String SESSION_NAME = "Personal Best Run";
    public static final String SESSION_DESCRIPTION = "Doing a run";

    private final GoogleFitAdapter googleFitAdapter;

    private String currentSessionID;
    private IFitnessSnapshot currentSnapshot;
    private boolean active = false;

    public ActiveFitnessService(GoogleFitAdapter googleFitAdapter)
    {
        this.googleFitAdapter = googleFitAdapter;
    }

    public ActiveFitnessSnapshot startRecording()
    {
        final long time = this.googleFitAdapter.getCurrentTime();

        this.currentSessionID = "personalBestRun";
        Session session = new Session.Builder()
                .setName(SESSION_NAME)
                .setIdentifier(this.currentSessionID)
                .setDescription(SESSION_DESCRIPTION)
                .setStartTime(time, TimeUnit.MILLISECONDS)
                .build();

        Task<Void> response = Fitness.getSessionsClient(
                this.googleFitAdapter.getActivity().getApplicationContext(),
                this.googleFitAdapter.getCurrentGoogleAccount())
                .startSession(session);

        this.active = true;
        //this.currentSnapshot = new ActiveFitnessService();
        return null;
    }

    public FitnessSnapshot stopRecording()
    {
        Task<List<Session>> response = Fitness.getSessionsClient(
                this.googleFitAdapter.getActivity().getApplicationContext(),
                this.googleFitAdapter.getCurrentGoogleAccount())
                .stopSession(this.currentSessionID);

        this.active = false;
        //this.currentSnapshot = new ImmutableFitnessSnapshot();
        return null;
    }

    public boolean isActive()
    {
        return this.active;
    }

    @Override
    public Callback<Iterable<IFitnessSnapshot>> getFitnessSnapshots(long startTime, long stopTime)
    {
        final Callback<Iterable<IFitnessSnapshot>> callback = new Callback<>();
        final Activity activity = this.googleFitAdapter.getActivity();
        final Context context = activity.getApplicationContext();
        final GoogleSignInAccount userAccount = this.googleFitAdapter.getCurrentGoogleAccount();

        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, stopTime, TimeUnit.MILLISECONDS)
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .read(DataType.TYPE_DISTANCE_DELTA)
                .setSessionName(SESSION_NAME)
                .build();

        Fitness.getSessionsClient(context, userAccount)
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

                                    snapshot.setDistanceTravelled(
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

        return callback;
    }

    @Override
    public Callback<IFitnessSnapshot> getFitnessSnapshot()
    {
        final Callback<IFitnessSnapshot> callback = new Callback<>();
        final Activity activity = this.googleFitAdapter.getActivity();
        final Context context = activity.getApplicationContext();
        final GoogleSignInAccount userAccount = this.googleFitAdapter.getCurrentGoogleAccount();

        if (this.currentSessionID == null)
            throw new IllegalStateException("Unable to find current session");

        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setSessionId(this.currentSessionID)
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .read(DataType.TYPE_DISTANCE_DELTA)
                .setSessionName(SESSION_NAME)
                .build();

        Fitness.getSessionsClient(context, userAccount)
                .readSession(readRequest)
                .addOnSuccessListener(new OnSuccessListener<SessionReadResponse>() {
                    @Override
                    public void onSuccess(SessionReadResponse sessionReadResponse) {
                        final FitnessSnapshot snapshot = new FitnessSnapshot();

                        final List<Session> sessions = sessionReadResponse.getSessions();
                        if (!sessions.isEmpty())
                        {
                            Session currentSession = sessions.get(0);
                            final List<DataSet> dataSets = sessionReadResponse.getDataSet(currentSession);
                            if (!dataSets.isEmpty())
                            {
                                DataSet dataSet = dataSets.get(0);
                                if (!dataSet.isEmpty())
                                {
                                    final DataPoint data = dataSet.getDataPoints().get(0);

                                    snapshot.setStartTime(data.getStartTime(TimeUnit.MILLISECONDS));
                                    snapshot.setStopTime(data.getEndTime(TimeUnit.MILLISECONDS));

                                    snapshot.setTotalSteps(
                                            data.getValue(Field.FIELD_STEPS).asInt());

                                    snapshot.setDistanceTravelled(
                                            data.getValue(Field.FIELD_DISTANCE).asFloat());
                                }
                            }
                        }

                        callback.resolve(snapshot);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Unable to get active step stats", e);

                        callback.resolve(null);
                    }
                });

        return callback;
    }
}
