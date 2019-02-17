package team30.personalbest.fitness.service;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
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
import team30.personalbest.fitness.GoogleFitAdapter;
import team30.personalbest.fitness.snapshot.FitnessSnapshot;
import team30.personalbest.fitness.snapshot.IFitnessSnapshot;

public class FitnessService implements IFitnessService
{
    public static final String TAG = "FitnessService";

    private final GoogleFitAdapter googleFitAdapter;

    public FitnessService(GoogleFitAdapter googleFitAdapter)
    {
        this.googleFitAdapter = googleFitAdapter;
    }

    @Override
    public Callback<Iterable<IFitnessSnapshot>> getFitnessSnapshots(long startTime, long stopTime)
    {
        final Callback<Iterable<IFitnessSnapshot>> callback = new Callback<>();
        final Activity activity = this.googleFitAdapter.getActivity();
        final GoogleSignInAccount userAccount = this.googleFitAdapter.getCurrentGoogleAccount();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, stopTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(activity, userAccount)
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        List<IFitnessSnapshot> snapshots = new ArrayList<>();

                        final List<DataSet> dataSets = dataReadResponse.getDataSets();
                        for(DataSet dataSet : dataSets)
                        {
                            for(DataPoint data : dataSet.getDataPoints())
                            {
                                FitnessSnapshot snapshot = new FitnessSnapshot();

                                snapshot.setStartTime(data.getStartTime(TimeUnit.MILLISECONDS));
                                snapshot.setStopTime(data.getEndTime(TimeUnit.MILLISECONDS));
                                snapshot.setTotalSteps(data.getValue(Field.FIELD_STEPS).asInt());
                                //snapshot.setDistanceTravelled(0);

                                snapshots.add(snapshot);
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
        final GoogleSignInAccount userAccount = this.googleFitAdapter.getCurrentGoogleAccount();

        Fitness.getHistoryClient(activity, userAccount)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {
                        final FitnessSnapshot snapshot = new FitnessSnapshot();

                        if (!dataSet.isEmpty())
                        {
                            final DataPoint data = dataSet.getDataPoints().get(0);
                            snapshot.setStartTime(data.getStartTime(TimeUnit.MILLISECONDS));
                            snapshot.setStopTime(data.getEndTime(TimeUnit.MILLISECONDS));
                            snapshot.setTotalSteps(data.getValue(Field.FIELD_STEPS).asInt());
                            //TODO: get dist!
                            //TODO: get speed!
                        }

                        callback.resolve(snapshot);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Failed to get daily step count", e);

                        callback.resolve(null);
                    }
                });

        return callback;
    }
}
