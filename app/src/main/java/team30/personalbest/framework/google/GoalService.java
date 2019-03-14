package team30.personalbest.framework.google;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataTypeCreateRequest;
import com.google.android.gms.fitness.request.DataUpdateRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import team30.personalbest.R;
import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.service.IGoalService;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.framework.snapshot.GoalSnapshot;
import team30.personalbest.framework.snapshot.IGoalSnapshot;
import team30.personalbest.util.Callback;

public class GoalService implements IGoalService, IGoogleService
{
	public static final String TAG = "GoalService";

	public static final String DATA_TYPE_GOAL_NAME = "team30.personalbest.goal";
	private final String DATA_SESSION_NAME = "GOAL";

	private GoogleFitnessAdapter googleFitnessAdapter;
	private GoogleApiClient googleApiClient;
	private DataSource dataSource;

	@Override
	public Callback<GoalService> initialize(GoogleFitnessAdapter googleFitnessAdapter)
	{
		final Callback<GoalService> callback = new Callback<>();

		this.googleFitnessAdapter = googleFitnessAdapter;
		this.googleApiClient = new GoogleApiClient.Builder(googleFitnessAdapter.getActivity())
				.addApi(Fitness.CONFIG_API)
				.addApi(Fitness.HISTORY_API)
				.addApi(Fitness.SESSIONS_API)
				.addApi(Fitness.RECORDING_API)
				.addApi(Fitness.SENSORS_API)
				.build();

		final GoalService goalService = this;
		this.googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
			@Override
			public void onConnected(@Nullable Bundle bundle)
			{
				Log.d(TAG, "Google API client connection has succeeded.");

				goalService.createDataSource(goalService.googleApiClient, googleFitnessAdapter.getActivity())
						.onResult(dataSource -> {
							goalService.dataSource = dataSource;
							goalService.getDataType(goalService.googleApiClient).onResult(dataType -> callback.resolve(goalService));
						});
			}

			@Override
			public void onConnectionSuspended(int i)
			{
				Log.d(TAG, "Google API client connection is suspended...");
			}
		});
		this.googleApiClient.registerConnectionFailedListener(connectionResult -> {
			Log.d(TAG, "Google API client connection has failed.");
			callback.reject();
		});
		this.googleApiClient.connect();

		return callback;
	}

	private Callback<DataSource> createDataSource(final GoogleApiClient googleApiClient, final Activity activity)
	{
		final Callback<DataSource> callback = new Callback<>();
		{
			this.getDataType(googleApiClient).onResult(dataType -> {
				final DataSource result = new DataSource.Builder()
						.setAppPackageName(activity)
						.setDataType(dataType)
						.setName(DATA_SESSION_NAME)
						.setType(DataSource.TYPE_RAW)
						.build();
				callback.resolve(result);
			});
		}
		return callback;
	}

	private Callback<DataType> getDataType(final GoogleApiClient googleApiClient)
	{
		final Callback<DataType> callback = new Callback<>();
		{
			Fitness.ConfigApi.readDataType(googleApiClient, DATA_TYPE_GOAL_NAME)
					.setResultCallback(dataTypeResult -> {
						if (dataTypeResult.getStatus().isSuccess())
						{
							Log.d(TAG, "Successfully found existing goal data type.");
							final DataType result = dataTypeResult.getDataType();
							callback.resolve(result);
						}
						else
						{
							Log.d(TAG, "Failed to find goal data type...creating a new one...");
							final DataTypeCreateRequest createRequest = new DataTypeCreateRequest.Builder()
									.setName(DATA_TYPE_GOAL_NAME)
									.addField("value", Field.FORMAT_INT32)
									.addField("day", Field.FORMAT_INT32)
									.build();

							Fitness.ConfigApi.createCustomDataType(googleApiClient, createRequest)
									.setResultCallback(dataTypeResult1 -> {
										final DataType result = dataTypeResult1.getDataType();
										callback.resolve(result);
									});
						}
					});
		}
		return callback;
	}

	@Override
	public Callback<IGoalSnapshot> getGoalSnapshot(IFitnessUser user, IFitnessClock clock)
	{
		final Callback<IGoalSnapshot> callback = new Callback<>();
		{
			this.getDataType(this.googleApiClient).onResult(dataType -> {
				this.googleFitnessAdapter.getCurrentGoogleAccount().onResult(lastSignedInAccount -> {
					if (lastSignedInAccount == null)
					{
						callback.reject();
						return;
					}

					final Activity activity = this.googleFitnessAdapter.getActivity();

					final long startTime = 1;
					final long stopTime = clock.getCurrentTime();

					final DataReadRequest readRequest = new DataReadRequest.Builder()
							.read(dataType)
							.setTimeRange(startTime, stopTime, TimeUnit.MILLISECONDS)
							.setLimit(1)
							.build();

					Fitness.getHistoryClient(activity, lastSignedInAccount)
							.readData(readRequest)
							.addOnSuccessListener(dataReadResponse -> {
								List<DataSet> dataSets = dataReadResponse.getDataSets();
								if (!dataSets.isEmpty())
								{
									List<DataPoint> dataPoints = dataSets.get(0).getDataPoints();
									if (!dataPoints.isEmpty())
									{
										DataPoint dataPoint = dataPoints.get(0);
										final GoalSnapshot goalSnapshot = new GoalSnapshot()
												.setGoalValue(dataPoint.getValue(dataPoint.getDataType().getFields().get(0)).asInt())
												.setGoalTime(dataPoint.getStartTime(TimeUnit.MILLISECONDS));
										callback.resolve(goalSnapshot);
										return;
									}
								}

								Log.w(TAG, "Unable to find goal");
								callback.reject();
							})
							.addOnFailureListener(e -> {
								Log.w(TAG, "Failed to find goal", e);
								callback.reject();
							});
				});
			});
		}
		return callback;
	}

	@Override
	public Callback<Iterable<IGoalSnapshot>> getGoalSnapshots(IFitnessUser user, IFitnessClock clock, long startTime, long stopTime)
	{
		final Callback<Iterable<IGoalSnapshot>> callback = new Callback<>();
		{
			this.getDataType(this.googleApiClient).onResult(dataType -> {
				this.googleFitnessAdapter.getCurrentGoogleAccount().onResult(lastSignedInAccount -> {
					if (lastSignedInAccount == null)
					{
						callback.reject();
						return;
					}

					final Activity activity = this.googleFitnessAdapter.getActivity();

					final DataReadRequest readRequest = new DataReadRequest.Builder()
							.read(dataType)
							.setTimeRange(startTime, stopTime + 1, TimeUnit.MILLISECONDS)
							.build();

					Fitness.getHistoryClient(activity, lastSignedInAccount)
							.readData(readRequest)
							.addOnSuccessListener(dataReadResponse -> {
								final List<IGoalSnapshot> goals = new ArrayList<>();
								final List<DataSet> dataSets = dataReadResponse.getDataSets();
								for (DataSet dataSet : dataSets)
								{
									for (DataPoint dataPoint : dataSet.getDataPoints())
									{
										try
										{
											final GoalSnapshot snapshot = new GoalSnapshot()
													.setGoalValue(dataPoint.getValue(dataPoint.getDataType().getFields().get(0)).asInt())
													.setGoalTime(dataPoint.getStartTime(TimeUnit.MILLISECONDS));
											goals.add(snapshot);
										}
										catch (Exception e)
										{
											Log.w(TAG, "Cannot find goal field value for data", e);
										}
									}
								}
								callback.resolve(goals);
							})
							.addOnFailureListener(e -> {
								Log.w(TAG, "Failed to find goal", e);
								callback.reject();
							});
				});
			});
		}
		return callback;
	}

	public Callback<IGoalSnapshot> setCurrentGoal(IFitnessUser user, IFitnessClock clock, int goalValue)
	{
		final Callback<IGoalSnapshot> callback = new Callback<>();
		{
			this.getDataType(this.googleApiClient).onResult(dataType -> {
				this.googleFitnessAdapter.getCurrentGoogleAccount().onResult(lastSignedInAccount -> {
					if (lastSignedInAccount == null)
					{
						callback.reject();
						return;
					}

					final Activity activity = this.googleFitnessAdapter.getActivity();

					final long startTime = clock.getCurrentTime();
					final long stopTime = startTime + 1;

					final DataSet dataSet = DataSet.create(this.dataSource);
					final DataPoint newGoal = dataSet.createDataPoint()
							.setTimeInterval(startTime, stopTime, TimeUnit.MILLISECONDS);
					newGoal.getValue(dataType.getFields().get(0)).setInt(goalValue);
					dataSet.add(newGoal);

					final DataUpdateRequest updateRequest = new DataUpdateRequest.Builder()
							.setDataSet(dataSet)
							.setTimeInterval(startTime, stopTime, TimeUnit.MILLISECONDS)
							.build();

					Fitness.getHistoryClient(activity, lastSignedInAccount)
							.updateData(updateRequest)
							.addOnSuccessListener(aVoid -> {
								Log.d(TAG, "Successfully updated goal value.");
								final GoalSnapshot goalSnapshot = new GoalSnapshot()
										.setGoalValue(goalValue)
										.setGoalTime(startTime);
								callback.resolve(goalSnapshot);
							})
							.addOnFailureListener(e -> {
								Log.w(TAG, "Failed to update goal value.", e);
								callback.reject();
							});
				});
			});
		}
		return callback;
	}
}
