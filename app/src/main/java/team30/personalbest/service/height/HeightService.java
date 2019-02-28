package team30.personalbest.service.height;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.concurrent.TimeUnit;

import team30.personalbest.service.IService;
import team30.personalbest.service.ServiceInitializer;
import team30.personalbest.service.fitness.IFitnessService;
import team30.personalbest.util.Callback;

public class HeightService implements IHeightService
{
	public static final String TAG = "HeightService";

	private Activity activity;
	private final IFitnessService fitnessService;

	public HeightService(IFitnessService fitnessService)
	{
		this.fitnessService = fitnessService;
	}

	@Nullable
	@Override
	public Callback<IService> onActivityCreate(ServiceInitializer serviceInitializer, Activity activity, Bundle savedInstanceState)
	{
		return this.initialize(activity);
	}

	@Nullable
	@Override
	public Callback<IService> onActivityResult(ServiceInitializer serviceInitializer, Activity activity, int requestCode, int resultCode, @Nullable Intent data)
	{
		return null;
	}

	@Override
	public Callback<IService> initialize(Activity activity)
	{
		this.activity = activity;
		return new Callback<>(this);
	}

	@Override
	public Callback<Float> setHeight(float height)
	{
		final Callback<Float> callback = new Callback<>();

		final Activity activity = this.activity;
		final IFitnessService fitnessService = this.fitnessService;

		final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity);
		if (lastSignedInAccount == null)
		{
			Log.w(TAG, "Unable to set height - unable to get google account");
			callback.resolve(null);
		}
		else
		{
			final DataSource dataSource = new DataSource.Builder()
					.setAppPackageName(activity)
					.setDataType(DataType.TYPE_HEIGHT)
					.setType(DataSource.TYPE_RAW)
					.build();

			final DataSet dataSet = DataSet.create(dataSource);
			final DataPoint dataPoint = dataSet.createDataPoint()
					.setTimestamp(fitnessService.getCurrentTime(), TimeUnit.MILLISECONDS)
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
		}
		return callback;
	}

	@Override
	public Callback<Float> getHeight()
	{
		final Callback<Float> callback = new Callback<>();

		final Activity activity = this.activity;
		final IFitnessService fitnessService = this.fitnessService;

		final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity);
		if (lastSignedInAccount == null)
		{
			Log.w(TAG, "Unable to get height - unable to get google account");
			callback.resolve(null);
		}
		else
		{
			final DataReadRequest dataReadRequest = new DataReadRequest.Builder()
					.read(DataType.TYPE_HEIGHT)
					.setTimeRange(1, fitnessService.getCurrentTime(), TimeUnit.MILLISECONDS)
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
							callback.resolve(null);
						}
					})
					.addOnFailureListener(e -> {
						Log.w(TAG, "Failed to fetch user's height", e);
						callback.resolve(null);
					});
		}
		return callback;
	}
}
