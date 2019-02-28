package team30.personalbest.service.goal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Consumer;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataTypeCreateRequest;
import com.google.android.gms.fitness.request.DataUpdateRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.DataTypeResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import team30.personalbest.service.IService;
import team30.personalbest.service.ServiceInitializer;
import team30.personalbest.service.fitness.IFitnessService;
import team30.personalbest.snapshot.GoalSnapshot;
import team30.personalbest.snapshot.IGoalSnapshot;
import team30.personalbest.util.Callback;

public class GoalService implements IGoalService
{
	public static final String TAG = "GoalService";
	public static final String DATA_TYPE_GOAL_NAME = "team30.personalbest.goal";

	private final String SESSION_NAME = "GOAL";

	private Activity activity;
	private GoogleApiClient googleApiClient;
	private DataSource dataSource;
	private IFitnessService fitnessService;

	public GoalService(IFitnessService fitnessService)
	{
		this.fitnessService = fitnessService;
	}

	@Override
	public Callback<IGoalSnapshot> setCurrentGoal(final int goalValue)
	{
		final Callback<IGoalSnapshot> callback = new Callback<>();
		final GoalService goalService = this;
		this.getDataType(this.googleApiClient).onResult(new Consumer<DataType>()
		{
			@Override
			public void accept(final DataType dataType)
			{
				final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(goalService.activity);
				if (lastSignedInAccount != null)
				{
					final long startTime = goalService.fitnessService.getCurrentTime();
					final long stopTime = startTime + 1;

					final DataSet dataSet = DataSet.create(goalService.dataSource);
					DataPoint newGoal = dataSet.createDataPoint()
							.setTimeInterval(startTime, stopTime, TimeUnit.MILLISECONDS);
					newGoal.getValue(dataType.getFields().get(0)).setInt(goalValue);
					dataSet.add(newGoal);

					final DataUpdateRequest updateRequest = new DataUpdateRequest.Builder()
							.setDataSet(dataSet)
							.setTimeInterval(startTime, stopTime, TimeUnit.MILLISECONDS)
							.build();

					Fitness.getHistoryClient(goalService.activity, lastSignedInAccount)
							.updateData(updateRequest)
							.addOnSuccessListener(new OnSuccessListener<Void>()
							{
								@Override
								public void onSuccess(Void aVoid)
								{
									Log.d(TAG, "Successfully updated goal value.");
									final GoalSnapshot goalSnapshot = new GoalSnapshot()
											.setGoalValue(goalValue)
											.setGoalTime(startTime);
									callback.resolve(goalSnapshot);
								}
							})
							.addOnFailureListener(new OnFailureListener()
							{
								@Override
								public void onFailure(@NonNull Exception e)
								{
									Log.w(TAG, "Failed to update goal value.", e);
									callback.resolve(null);
								}
							});
				}
				else
				{
					throw new IllegalStateException("Unable to find google account to set goal");
				}
			}
		});

		return callback;
	}

	@Override
	public Callback<IGoalSnapshot> getGoalSnapshot()
	{
		final Callback<IGoalSnapshot> callback = new Callback<>();
		final GoalService goalService = this;
		this.getDataType(this.googleApiClient).onResult(new Consumer<DataType>()
		{
			@Override
			public void accept(DataType dataType)
			{
				final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(goalService.activity);

				if (lastSignedInAccount != null)
				{
					final long startTime = 1;
					final long stopTime = goalService.fitnessService.getCurrentTime();

					final DataReadRequest readRequest = new DataReadRequest.Builder()
							.read(dataType)
							.setTimeRange(startTime, stopTime, TimeUnit.MILLISECONDS)
							.setLimit(1)
							.build();

					Fitness.getHistoryClient(goalService.activity, lastSignedInAccount)
							.readData(readRequest)
							.addOnSuccessListener(new OnSuccessListener<DataReadResponse>()
							{
								@Override
								public void onSuccess(DataReadResponse dataReadResponse)
								{
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
									callback.resolve(null);
								}
							})
							.addOnFailureListener(new OnFailureListener()
							{
								@Override
								public void onFailure(@NonNull Exception e)
								{
									Log.w(TAG, "Failed to find goal", e);
									callback.resolve(null);
								}
							});
				}
				else
				{
					Log.w(TAG, "Unable to find google account for goal data");
					callback.resolve(null);
				}
			}
		});
		return callback;
	}

	@Override
	public Callback<Iterable<IGoalSnapshot>> getGoalSnapshots(final long startTime, final long stopTime)
	{
		final Callback<Iterable<IGoalSnapshot>> callback = new Callback<>();
		final GoalService goalService = this;
		this.getDataType(this.googleApiClient).onResult(new Consumer<DataType>()
		{
			@Override
			public void accept(DataType dataType)
			{
				final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(goalService.activity);
				if (lastSignedInAccount != null)
				{
					final DataReadRequest readRequest = new DataReadRequest.Builder()
							.read(dataType)
							.setTimeRange(startTime, stopTime, TimeUnit.MILLISECONDS)
							.build();

					Fitness.getHistoryClient(goalService.activity, lastSignedInAccount)
							.readData(readRequest)
							.addOnSuccessListener(new OnSuccessListener<DataReadResponse>()
							{
								@Override
								public void onSuccess(DataReadResponse dataReadResponse)
								{
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
								}
							})
							.addOnFailureListener(new OnFailureListener()
							{
								@Override
								public void onFailure(@NonNull Exception e)
								{
									Log.w(TAG, "Failed to find goal", e);
									callback.resolve(null);
								}
							});
				}
				else
				{
					Log.w(TAG, "Unable to find google account for goal data");
					callback.resolve(null);
				}
			}
		});

		return callback;
	}

	@Override
	public Callback<IGoalSnapshot> isCurrentGoalAchieved(final IFitnessService fitnessService)
	{
		final Callback<IGoalSnapshot> callback = new Callback<>();
		return callback;
	}

	@Nullable
	@Override
	public Callback<IService> onActivityCreate(ServiceInitializer serviceInitializer, final Activity activity, Bundle savedInstanceState)
	{
		this.activity = activity;

		final Callback<IService> callback = new Callback<>();
		final GoalService goalService = this;

		this.googleApiClient = new GoogleApiClient.Builder(activity)
				.addApi(Fitness.CONFIG_API)
				.addApi(Fitness.HISTORY_API)
				.addApi(Fitness.SESSIONS_API)
				.addApi(Fitness.RECORDING_API)
				.addApi(Fitness.SENSORS_API)
				.build();

		this.googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks()
		{
			@Override
			public void onConnected(@Nullable Bundle bundle)
			{
				Log.d(TAG, "Google API client connection has succeeded.");

				goalService.createDataSource(goalService.googleApiClient, activity)
						.onResult(new Consumer<DataSource>()
						{
							@Override
							public void accept(DataSource dataSource)
							{
								goalService.dataSource = dataSource;
								goalService.getDataType(goalService.googleApiClient).onResult(new Consumer<DataType>() {
									@Override
									public void accept(DataType dataType)
									{
										callback.resolve(goalService);
									}
								});
							}
						});
			}

			@Override
			public void onConnectionSuspended(int i)
			{
				Log.d(TAG, "Google API client connection is suspended...");
			}
		});
		this.googleApiClient.registerConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener()
		{
			@Override
			public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
			{
				Log.d(TAG, "Google API client connection has failed.");
			}
		});
		this.googleApiClient.connect();

		return callback;
	}

	@Nullable
	@Override
	public Callback<IService> onActivityResult(ServiceInitializer serviceInitializer, Activity activity, int requestCode, int resultCode, @Nullable Intent data)
	{
		return null;
	}

	private Callback<DataSource> createDataSource(final GoogleApiClient googleApiClient, final Activity activity)
	{
		final Callback<DataSource> callback = new Callback<>();
		this.getDataType(googleApiClient).onResult(new Consumer<DataType>()
		{
			@Override
			public void accept(DataType dataType)
			{
				final DataSource result = new DataSource.Builder()
						.setAppPackageName(activity)
						.setDataType(dataType)
						.setName(SESSION_NAME)
						.setType(DataSource.TYPE_RAW)
						.build();
				callback.resolve(result);
			}
		});
		return callback;
	}

	private Callback<DataType> getDataType(final GoogleApiClient googleApiClient)
	{
		final Callback<DataType> callback = new Callback<>();
		Fitness.ConfigApi.readDataType(googleApiClient, DATA_TYPE_GOAL_NAME)
				.setResultCallback(new ResultCallback<DataTypeResult>()
				{
					@Override
					public void onResult(@NonNull DataTypeResult dataTypeResult)
					{
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
									.setResultCallback(new ResultCallback<DataTypeResult>()
									{
										@Override
										public void onResult(@NonNull DataTypeResult dataTypeResult)
										{
											final DataType result = dataTypeResult.getDataType();
											callback.resolve(result);
										}
									});
						}
					}
				});
		return callback;
	}
}
