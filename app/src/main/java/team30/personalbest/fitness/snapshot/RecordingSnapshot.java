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
import team30.personalbest.fitness.service.IGoogleFitDataHandler;

public class RecordingSnapshot extends FitnessSnapshot implements IRecordingFitnessSnapshot, IGoogleFitDataHandler
{
    public static final String TAG = "RecordingSnapshot";
    public static final String SESSION_DATA_NAME = "PersonalBest-steps";

    private final List<OnRecordingSnapshotUpdateListener> listeners = new ArrayList<>();
    private final GoogleFitAdapter googleFitAdapter;

    private int initialSteps = 0;
    private float initialDistance = 0;

    public RecordingSnapshot(GoogleFitAdapter googleFitAdapter)
    {
        this.googleFitAdapter = googleFitAdapter;
        this.setStartTime(this.googleFitAdapter.getCurrentTime());
    }

    @Override
    public IRecordingFitnessSnapshot addOnRecordingSnapshotUpdateListener(OnRecordingSnapshotUpdateListener listener)
    {
        this.listeners.add(listener);
        return this;
    }

    @Override
    public DataPoint onProcessDataPoint(DataSource dataSource, DataSet dataSet,
                                        DataPoint dataPoint, DataType dataType)
    {
        Log.d(TAG, "Updating for data point..." + dataPoint.toString());

        final DataPoint dst = dataSet.createDataPoint();

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
            long millis = System.currentTimeMillis();
            dst.setTimeInterval(millis, millis + 1000, TimeUnit.MILLISECONDS);
            dst.getValue(Field.FIELD_STEPS).setInt(result);
        }
        else if (dataPoint.getDataType().equals(DataType.TYPE_DISTANCE_CUMULATIVE))
        {
            float result = dataPoint.getValue(Field.FIELD_DISTANCE).asFloat();
            this.setSpeed(result);

            long millis = System.currentTimeMillis();
            dst.setTimeInterval(millis, millis + 1000, TimeUnit.MILLISECONDS);
            dst.getValue(Field.FIELD_DISTANCE).setFloat(result);
        }
        else
        {
            Log.w(TAG, "Found unknown data point.");
        }

        this.setStopTime(dataPoint.getEndTime(TimeUnit.MILLISECONDS));

        for(OnRecordingSnapshotUpdateListener listener : this.listeners)
        {
            listener.onRecordingSnapshotUpdate(this);
        }

        return dst;
    }
}
