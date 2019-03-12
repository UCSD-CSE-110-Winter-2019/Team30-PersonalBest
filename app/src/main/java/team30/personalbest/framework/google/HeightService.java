package team30.personalbest.framework.google;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;

import java.util.concurrent.TimeUnit;

import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.service.IHeightService;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.util.Callback;

public class HeightService implements IHeightService, IGoogleService
{
	public static final String TAG = "HeightService";

	private GoogleFitnessAdapter googleFitnessAdapter;

	@Override
	public Callback<HeightService> initialize(GoogleFitnessAdapter googleFitnessAdapter)
	{
		final Callback<HeightService> callback = new Callback<>();
		{
			this.googleFitnessAdapter = googleFitnessAdapter;
			callback.resolve(this);
		}
		return callback;
	}

	@Override
	public Callback<Float> getHeight(IFitnessUser user, IFitnessClock clock)
	{
		final Callback<Float> callback = new Callback<>();
		{
			this.googleFitnessAdapter.getCurrentGoogleAccount().onResult(lastSignedInAccount -> {
				if (lastSignedInAccount == null)
				{
					callback.reject();
					return;
				}

				final Activity activity = this.googleFitnessAdapter.getActivity();

				final DataReadRequest dataReadRequest = new DataReadRequest.Builder()
						.read(DataType.TYPE_HEIGHT)
						.setTimeRange(1, clock.getCurrentTime(), TimeUnit.MILLISECONDS)
						.setLimit(1)
						.build();

				Fitness.getHistoryClient(activity, lastSignedInAccount)
						.readData(dataReadRequest)
						.addOnSuccessListener(dataReadResponse -> {
							Log.i(TAG, "Successfully received user's height");

							final DataSet dataSet = dataReadResponse.getDataSet(DataType.TYPE_HEIGHT);
							if (!dataSet.isEmpty())
							{
								final DataPoint data = dataSet.getDataPoints().get(0);
								final float height = data.getValue(Field.FIELD_HEIGHT).asFloat();
								callback.resolve(height);
							}
							else
							{
								callback.reject();
							}
						})
						.addOnFailureListener(e -> {
							Log.w(TAG, "Failed to fetch user's height", e);
							callback.reject();
						});
			});
		}
		return callback;
	}

	public Callback<Float> setHeight(IFitnessUser user, IFitnessClock clock, float height)
	{
		final Callback<Float> callback = new Callback<>();
		{
			this.googleFitnessAdapter.getCurrentGoogleAccount().onResult(lastSignedInAccount -> {
				if (lastSignedInAccount == null)
				{
					callback.reject();
					return;
				}

				final Activity activity = this.googleFitnessAdapter.getActivity();

				final DataSource dataSource = new DataSource.Builder()
						.setAppPackageName(activity)
						.setDataType(DataType.TYPE_HEIGHT)
						.setType(DataSource.TYPE_RAW)
						.build();

				final DataSet dataSet = DataSet.create(dataSource);
				final DataPoint dataPoint = dataSet.createDataPoint()
						.setTimestamp(clock.getCurrentTime(), TimeUnit.MILLISECONDS)
						.setFloatValues(height);
				dataSet.add(dataPoint);

				Fitness.getHistoryClient(activity, lastSignedInAccount)
						.insertData(dataSet)
						.addOnSuccessListener(aVoid -> {
							Log.i(TAG, "Successfully registered user's height");
							callback.resolve(height);
						})
						.addOnFailureListener(e -> {
							Log.w(TAG, "Failed to register user's height", e);
							callback.resolve(null);
						});
			});
		}
		return callback;
	}
}
