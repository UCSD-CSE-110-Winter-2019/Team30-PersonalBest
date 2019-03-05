package team30.personalbest.framework.google.recorder;

import android.util.Log;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import team30.personalbest.framework.snapshot.FitnessSnapshot;
import team30.personalbest.framework.snapshot.IRecordingFitnessSnapshot;
import team30.personalbest.framework.snapshot.OnRecordingSnapshotUpdateListener;

public class RecordingSnapshot extends FitnessSnapshot implements IRecordingFitnessSnapshot, IGoogleFitDataHandler
{
	public static final String TAG = "RecordingSnapshot";

	private final List<OnRecordingSnapshotUpdateListener> listeners = new ArrayList<>();

	private int stepsTaken = 0;

	public RecordingSnapshot(long startTime)
	{
		this.setStartTime(startTime);
		this.setStopTime(startTime);
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
		final long startTime = dataPoint.getStartTime(TimeUnit.MILLISECONDS);
		if (this.getStartTime() > startTime) this.setStartTime(startTime);
		final long endTime = dataPoint.getEndTime(TimeUnit.MILLISECONDS);
		if (this.getStopTime() < endTime) this.setStopTime(endTime);

		final DataPoint dst = dataSet.createDataPoint();

		if (dataPoint.getDataType().equals(DataType.TYPE_STEP_COUNT_DELTA))
		{
			int result = dataPoint.getValue(Field.FIELD_STEPS).asInt();
			Log.d(TAG, "Found " + result + " steps for data point...");
			if (result <= 0) return null;

			this.stepsTaken += result;
			this.setTotalSteps(this.stepsTaken);

			dst.setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
			dst.getValue(Field.FIELD_STEPS).setInt(result);
		}
		else
		{
			Log.w(TAG, "Found unknown data point.");
			return null;
		}

		for (OnRecordingSnapshotUpdateListener listener : this.listeners)
		{
			listener.onRecordingSnapshotUpdate(this);
		}

		return dst;
	}
}
