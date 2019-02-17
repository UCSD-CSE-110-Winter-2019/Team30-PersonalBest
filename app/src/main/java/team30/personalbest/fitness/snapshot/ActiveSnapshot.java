package team30.personalbest.fitness.snapshot;

import android.util.Log;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import team30.personalbest.fitness.service.GoogleFitAdapter;
import team30.personalbest.fitness.service.OnRecordDataUpdateListener;

public class ActiveSnapshot extends FitnessSnapshot implements IActiveFitnessSnapshot, OnRecordDataUpdateListener
{
    public static final String TAG = "ActiveSnapshot";
    public static final String SESSION_DATA_NAME = "PersonalBest-steps";

    private final List<OnActiveSnapshotUpdateListener> listeners = new ArrayList<>();
    private final GoogleFitAdapter googleFitAdapter;

    private int initialSteps = 0;
    private float initialDistance = 0;

    public ActiveSnapshot(GoogleFitAdapter googleFitAdapter)
    {
        this.googleFitAdapter = googleFitAdapter;
        this.setStartTime(this.googleFitAdapter.getCurrentTime());
    }

    @Override
    public IActiveFitnessSnapshot addOnActiveSnapshotUpdate(OnActiveSnapshotUpdateListener listener)
    {
        this.listeners.add(listener);
        return this;
    }

    @Override
    public void onRecordDataUpdate(DataSource dataSource, DataSet dataSet, DataPoint dataPoint, DataType dataType)
    {
        Log.d(TAG, "Updating for data point..." + dataPoint.toString());
        if (dataPoint.getDataType().equals(DataType.TYPE_STEP_COUNT_CUMULATIVE))
        {
            int result = dataPoint.getValue(Field.FIELD_STEPS).asInt();
            if (result > 0 && this.initialSteps == 0)
            {
                this.initialSteps = result;
            }
            else
            {
                this.setTotalSteps(result - this.initialSteps);
            }

            /*
            DataPoint newDataPoint = dataSet.createDataPoint()
                    .setTimeInterval(this.getStartTime(), this.getStopTime(), TimeUnit.MILLISECONDS)
                    .setTimestamp(this.getStopTime(), TimeUnit.MILLISECONDS);
            newDataPoint.getValue(Field.FIELD_STEPS).setInt(result);
            dataSet.add(newDataPoint);
            */
        }
        else if (dataPoint.getDataType().equals(DataType.TYPE_DISTANCE_CUMULATIVE))
        {
            float result = dataPoint.getValue(Field.FIELD_DISTANCE).asFloat();
            this.setSpeed(result);

            /*
            DataPoint newDataPoint = dataSet.createDataPoint()
                    .setTimeInterval(this.getStartTime(), this.getStopTime(), TimeUnit.MILLISECONDS)
                    .setTimestamp(this.getStopTime(), TimeUnit.MILLISECONDS);
            newDataPoint.getValue(Field.FIELD_DISTANCE).setFloat(result);
            dataSet.add(newDataPoint);
            */
        }
        else
        {
            Log.w(TAG, "Found unknown data point.");
        }
        this.setStopTime(dataPoint.getEndTime(TimeUnit.MILLISECONDS));

        for(OnActiveSnapshotUpdateListener listener : this.listeners)
        {
            listener.onActiveSnapshotUpdate(this);
        }
    }
}
