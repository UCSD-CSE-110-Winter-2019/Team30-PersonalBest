package team30.personalbest.framework.google.recorder;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;

import java.util.concurrent.TimeUnit;

import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.google.GoogleFitnessAdapter;

public class GoogleFitDataRecorder
{
	public static final String TAG = "GoogleFitDataRecorder";
	public static final String DATASET_NAME_PREFIX = "PersonalBestData-";

	private final GoogleFitnessAdapter googleFitnessAdapter;

	private final DataType dataType;
	private DataSource dataSource;
	private DataSet dataSet;

	private IFitnessClock clock;
	private IGoogleFitDataHandler handler;

	public GoogleFitDataRecorder(GoogleFitnessAdapter googleFitnessAdapter, DataType dataType, int samplingRate)
	{
		if (samplingRate <= 0)
			throw new IllegalArgumentException("Sampling rate must be a positive integer");

		this.googleFitnessAdapter = googleFitnessAdapter;

		final Activity activity = googleFitnessAdapter.getActivity();

		this.dataType = dataType;
		this.dataSource = new DataSource.Builder()
				.setName(DATASET_NAME_PREFIX + dataType.getName())
				.setType(DataSource.TYPE_RAW)
				.setDataType(this.dataType)
				.setAppPackageName(activity)
				.build();

		final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity);
		if (lastSignedInAccount != null)
		{
			final SensorRequest sensorRequest = new SensorRequest.Builder()
					.setDataType(this.dataType)
					.setSamplingRate(samplingRate, TimeUnit.SECONDS)
					.build();

			Fitness.getSensorsClient(activity, lastSignedInAccount)
					.add(sensorRequest, this::handleDataPoint)
					.addOnSuccessListener(aVoid -> Log.i(TAG, "Successfully registered sensor data listener for type " + this.dataType.toString()))
					.addOnFailureListener(e -> Log.w(TAG, "Failed to register sensor data listener for type" + this.dataType.toString(), e));
		}
		else
		{
			throw new IllegalStateException("Unable to find user account for recorder.");
		}
	}

	public void start(IFitnessClock clock, IGoogleFitDataHandler handler)
	{
		this.clock = clock;
		this.handler = handler;
		this.dataSet = DataSet.create(this.dataSource);
	}

	public DataSet stop()
	{
		this.handler = null;

		final DataSet result = this.dataSet;
		this.dataSet = null;

		return result;
	}

	private void handleDataPoint(DataPoint dataPoint)
	{
		Log.d(TAG, "...found active data point...");
		if (dataPoint.getDataType().equals(this.dataType))
		{
			Log.d(TAG, "Processing data point for type " + this.dataType.getName() + "...");

			if (this.handler != null)
			{
				long currentTime = this.clock.getCurrentTime();
				long dataPointTime = dataPoint.getEndTime(TimeUnit.MILLISECONDS);
				dataPoint.setTimeInterval(currentTime, Math.max(dataPointTime, currentTime + 1000), TimeUnit.MILLISECONDS);
				final DataPoint result = this.handler.onProcessDataPoint(
						this.dataSource, this.dataSet, dataPoint, this.dataType);
				Log.d(TAG, "Appending data point " + result + "...");
				if (result != null)
				{
					this.dataSet.add(result);
				}
			}
		}
		else
		{
			Log.w(TAG, "Found unknown data point of type " + dataPoint.getDataType().toString());
		}
	}
}
