package team30.personalbest.fitness.service;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.concurrent.TimeUnit;

public class GoogleFitDataRecorder implements OnDataPointListener
{
    public static final String TAG = "GoogleFitDataRecorder";
    public static final String DATASET_NAME_PREFIX = "PersonalBestData-";

    private final Activity activity;
    private final DataType dataType;
    private final int samplingRate;

    private DataSource dataSource;
    private DataSet dataSet;
    private OnRecordDataUpdateListener listener;

    public GoogleFitDataRecorder(Activity activity, DataType dataType, int samplingRate, OnRecordDataUpdateListener listener)
    {
        if (samplingRate <= 0) throw new IllegalArgumentException("Sampling rate must be a positive integer");

        this.activity = activity;
        this.dataType = dataType;
        this.samplingRate = samplingRate;
        this.listener = listener;

        final String packageName = activity.getApplicationContext().getPackageName();
        this.dataSource = new DataSource.Builder()
                .setAppPackageName(packageName)
                .setDataType(this.dataType)
                .setName(DATASET_NAME_PREFIX + dataType.getName())
                .setType(DataSource.TYPE_RAW)
                .build();
        this.dataSet = DataSet.create(this.dataSource);
    }

    @Override
    public void onDataPoint(DataPoint dataPoint)
    {
        if (dataPoint.getDataType().equals(this.dataType))
        {
            Log.d(TAG, "Processing data point for type " + this.dataType.getName() + "...");

            try
            {
                this.listener.onRecordDataUpdate(this.dataSource, this.dataSet, dataPoint, this.dataType);
            }
            catch (Exception e)
            {
                Log.w(TAG, "Failed to process recording data", e);
            }
        }
        else
        {
            Log.w(TAG, "Found unknown data point of type " + dataPoint.getDataType().toString());
        }
    }

    public void start()
    {
        final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this.activity);
        if (lastSignedInAccount != null)
        {
            Fitness.getSensorsClient(this.activity, lastSignedInAccount)
                    .add(new SensorRequest.Builder()
                                    .setDataType(this.dataType)
                                    .setSamplingRate(this.samplingRate, TimeUnit.SECONDS)
                                    .build(),
                            this)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "Successfully registered sensor data listener for type " + GoogleFitDataRecorder.this.dataType.toString());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Failed to register sensor data listener for type" + GoogleFitDataRecorder.this.dataType.toString(), e);
                        }
                    });
        }
        else
        {
            throw new IllegalStateException("Unable to find user account for recorder.");
        }
    }

    public DataSet stop()
    {
        final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this.activity);
        if (lastSignedInAccount != null)
        {
            Fitness.getSensorsClient(this.activity, lastSignedInAccount)
                    .remove(this)
                    .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            Log.i(TAG, "Successfully removed sensor data listeners for type " + GoogleFitDataRecorder.this.dataType.toString());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Failed to remove sensor data listeners for type " + GoogleFitDataRecorder.this.dataType.toString(), e);
                        }
                    });
        }
        else
        {
            throw new IllegalStateException("Unable to find user account for recorder.");
        }

        return this.dataSet;
    }
}
