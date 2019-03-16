package team30.personalbest.framework.google;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import team30.personalbest.framework.IFitnessAdapter;
import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.service.IFitnessService;
import team30.personalbest.framework.snapshot.FitnessSnapshot;
import team30.personalbest.framework.snapshot.IFitnessSnapshot;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.util.Callback;

public class FitnessService implements IFitnessService, IGoogleService
{
	public static final String TAG = "FitnessService";

	private GoogleFitnessAdapter googleFitnessAdapter;

	@Override
	public Callback<FitnessService> initialize(IFitnessAdapter googleFitnessAdapter)
	{
		final Callback<FitnessService> callback = new Callback<>();
		{
			this.googleFitnessAdapter = (GoogleFitnessAdapter) googleFitnessAdapter;

			this.googleFitnessAdapter.getCurrentGoogleAccount().onResult(lastSignedInAccount -> {
				if (lastSignedInAccount == null)
				{
					callback.reject();
					return;
				}

				final Activity activity = googleFitnessAdapter.getActivity();

				//Record steps passively...
				Fitness.getRecordingClient(activity, lastSignedInAccount)
						.subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
						.addOnSuccessListener(aVoid -> Log.i(TAG, "Successfully subscribed google recording services for passive counting."))
						.addOnFailureListener(e -> Log.w(TAG, "Failed to subscribe to google recording services for passive counting!", e));

				callback.resolve(this);
			});
		}
		return callback;
	}

	@Override
	public Callback<Integer> getDailySteps(IFitnessUser user, IFitnessClock clock, long dayTime)
	{
		final Callback<Integer> callback = new Callback<>();
		{
			this.googleFitnessAdapter.getCurrentGoogleAccount().onResult(lastSignedInAccount -> {
				if (lastSignedInAccount == null)
				{
					callback.reject();
					return;
				}

				final Activity activity = this.googleFitnessAdapter.getActivity();

				Fitness.getHistoryClient(activity, lastSignedInAccount)
						.readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
						.addOnSuccessListener(dataSet -> {
							Log.d(TAG, "Retrieving daily steps..." + dataSet.toString());
							if (!dataSet.isEmpty())
							{
								final DataPoint dataPoint = dataSet.getDataPoints().get(dataSet.getDataPoints().size() - 1);
								callback.resolve(dataPoint.getValue(Field.FIELD_STEPS).asInt());
							}
							else
							{
								callback.reject();
							}
						})
						.addOnFailureListener(e -> {
							Log.w(TAG, "Failed to retrieve daily steps.", e);
							callback.reject();
						});

				/*
				final long midnightTime = IFitnessClock.getMidnightOfDayTime(dayTime);
				final DataReadRequest readRequest = new DataReadRequest.Builder()
						.aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
						.bucketByTime(1, TimeUnit.DAYS)
						.setTimeRange(midnightTime, dayTime, TimeUnit.MILLISECONDS)
						.setLimit(1)
						.build();
				Fitness.getHistoryClient(activity, lastSignedInAccount)
						.readData(readRequest)
						.addOnSuccessListener(dataReadResponse -> {
							if (!dataReadResponse.getDataSets().isEmpty())
							{
								final DataSet dataSet = dataReadResponse.getDataSets().get(dataReadResponse.getDataSets().size() - 1);
								Log.d(TAG, "Retrieving daily steps..." + dataSet.toString());
								if (!dataSet.isEmpty())
								{
									final DataPoint dataPoint = dataSet.getDataPoints().get(dataSet.getDataPoints().size() - 1);
									callback.resolve(dataPoint.getValue(Field.FIELD_STEPS).asInt());
									return;
								}
							}
							callback.reject();
						})
						.addOnFailureListener(e -> {
							Log.w(TAG, "Failed to retrieve daily steps.", e);
							callback.reject();
						});
				*/
			});
		}
		return callback;
	}

	@Override
	public Callback<IFitnessSnapshot> getFitnessSnapshot(IFitnessUser user, IFitnessClock clock)
	{
		final Callback<IFitnessSnapshot> callback = new Callback<>();
		{
			this.googleFitnessAdapter.getCurrentGoogleAccount().onResult(lastSignedInAccount -> {
				if (lastSignedInAccount == null)
				{
					callback.reject();
					return;
				}

				final Activity activity = this.googleFitnessAdapter.getActivity();

				final long currentTime = clock.getCurrentTime();
				final long midnightTime = IFitnessClock.getMidnightOfDayTime(currentTime);
				final DataReadRequest readRequest = new DataReadRequest.Builder()
						.aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
						.bucketByTime(1, TimeUnit.DAYS)
						.setTimeRange(midnightTime, currentTime, TimeUnit.MILLISECONDS)
						.setLimit(1)
						.build();

				Fitness.getHistoryClient(activity, lastSignedInAccount)
						.readData(readRequest)
						.addOnSuccessListener(dataReadResponse -> {
							if (!dataReadResponse.getDataSets().isEmpty())
							{
								final DataSet dataSet = dataReadResponse.getDataSets().get(0);
								Log.d(TAG, "Retrieving fitness data..." + dataSet.toString());
								if (!dataSet.isEmpty())
								{
									final DataPoint dataPoint = dataSet.getDataPoints().get(0);
									final long startTime = dataPoint.getStartTime(TimeUnit.MILLISECONDS);
									final long stopTime = dataPoint.getEndTime(TimeUnit.MILLISECONDS);
									Log.d(TAG, "..." + dataPoint + "...");

									this.getRecordedSteps(startTime, stopTime)
											.onResult(integer -> callback.resolve(
													new FitnessSnapshot()
															.setTotalSteps(dataPoint.getValue(Field.FIELD_STEPS).asInt())
															.setRecordedSteps(integer == null ? 0 : integer)
															.setStartTime(startTime)
															.setStopTime(stopTime)));
									return;
								}
							}
							callback.reject();
						})
						.addOnFailureListener(e -> {
							Log.w(TAG, "Failed to retrieve fitness data.", e);
							callback.reject();
						});
			});
		}
		return callback;
	}

	@Override
	public Callback<Iterable<IFitnessSnapshot>> getFitnessSnapshots(IFitnessUser user, IFitnessClock clock, long startTime, long stopTime)
	{
		final Callback<Iterable<IFitnessSnapshot>> callback = new Callback<>();
		{
			this.googleFitnessAdapter.getCurrentGoogleAccount().onResult(lastSignedInAccount -> {
				if (lastSignedInAccount == null)
				{
					callback.reject();
					return;
				}
				if (startTime > stopTime)
				{
					callback.reject();
					return;
				}
				if (startTime < 0)
				{
					callback.reject();
					return;
				}

				Log.d(TAG, "Getting multiple fitness data between " +
						startTime + " to " +
						stopTime + ", which is (" +
						(stopTime - startTime) + ")");

				final Activity activity = this.googleFitnessAdapter.getActivity();

				final DataSource estimatedSteps = new DataSource.Builder()
						.setDataType(DataType.TYPE_STEP_COUNT_DELTA)
						.setType(DataSource.TYPE_DERIVED)
						.setStreamName("estimated_steps")
						.setAppPackageName("com.google.android.gms")
						.build();

				final DataReadRequest readRequest = new DataReadRequest.Builder()
						.read(DataType.AGGREGATE_STEP_COUNT_DELTA)
						//.aggregate(estimatedSteps, DataType.AGGREGATE_STEP_COUNT_DELTA)
						.setTimeRange(startTime, stopTime + 1, TimeUnit.MILLISECONDS)
						.bucketByTime(1, TimeUnit.DAYS)
						.enableServerQueries()
						.build();

				Fitness.getHistoryClient(activity, lastSignedInAccount)
						.readData(readRequest)
						.addOnSuccessListener(dataReadResponse -> {
							Log.d(TAG, "Retrieving multiple fitness data...");
							final List<FitnessSnapshot> result = new ArrayList<>();
							final List<Bucket> buckets = dataReadResponse.getBuckets();
							for (Bucket bucket : buckets)
							{
								long bucketStartTime = bucket.getStartTime(TimeUnit.MILLISECONDS);
								long bucketEndTime = bucket.getEndTime(TimeUnit.MILLISECONDS);
								final List<DataSet> dataSets = bucket.getDataSets();
								Log.d(TAG, "Data: " + dataSets.size());
								for (DataSet dataSet : dataSets)
								{
									Log.d(TAG, "..." + dataSet.toString());
									for (DataPoint data : dataSet.getDataPoints())
									{
										Log.d(TAG, "... ..." + data.toString());
										final FitnessSnapshot snapshot = new FitnessSnapshot()
												.setTotalSteps(data.getValue(Field.FIELD_STEPS).asInt())
												.setStartTime(bucketStartTime)
												.setStopTime(bucketEndTime);
										result.add(snapshot);
									}
								}
							}
							this.getRecordedStatsForSnapshots(result).onResult(aVoid -> callback.resolve(new ArrayList<>(result)));
						})
						.addOnFailureListener(e -> {
							Log.w(TAG, "Failed to retrieve fitness data for range.", e);
							callback.reject();
						});
			});
		}
		return callback;
	}

	private Callback<Void> getRecordedStatsForSnapshots(Iterable<FitnessSnapshot> iterable)
	{
		final Callback<Void> callback = new Callback<>();
		{
			this.getRecordedStatsForSnapshotsImpl(iterable.iterator(), callback);
		}
		return callback;
	}

	private void getRecordedStatsForSnapshotsImpl(Iterator<FitnessSnapshot> iterator, Callback<Void> callback)
	{
		if (!iterator.hasNext())
		{
			callback.resolve(null);
			return;
		}

		FitnessSnapshot fitnessSnapshot = iterator.next();
		this.getRecordedSteps(fitnessSnapshot.getStartTime(), fitnessSnapshot.getStopTime()).onResult(integer -> {
			if (integer != null)
			{
				fitnessSnapshot.setRecordedSteps(integer);
			}
			this.getRecordedStatsForSnapshotsImpl(iterator, callback);
		});
	}

	private Callback<Integer> getRecordedSteps(long startTime, long stopTime)
	{
		final Callback<Integer> callback = new Callback<>();
		{
			final SessionReadRequest sessionReadRequest = new SessionReadRequest.Builder()
					.read(DataType.TYPE_STEP_COUNT_DELTA)
					.setSessionName(GoogleFitnessAdapter.RECORDING_SESSION_NAME)
					.setTimeInterval(startTime, stopTime + 1, TimeUnit.MILLISECONDS)
					.build();

			final Activity activity = this.googleFitnessAdapter.getActivity();

			this.googleFitnessAdapter.getCurrentGoogleAccount().onResult(lastSignedInAccount -> {
				Fitness.getSessionsClient(activity, lastSignedInAccount)
						.readSession(sessionReadRequest)
						.addOnSuccessListener(sessionReadResponse -> {
							int sessionStepsTaken = 0;
							final List<Session> sessions = sessionReadResponse.getSessions();
							for (Session session : sessions)
							{
								final List<DataSet> dataSets = sessionReadResponse.getDataSet(session);
								if (dataSets.isEmpty()) continue;

								Log.w(TAG, "...found recorded stats...");

								for (DataSet dataSet : dataSets)
								{
									for (DataPoint data : dataSet.getDataPoints())
									{
										sessionStepsTaken += data.getValue(Field.FIELD_STEPS).asInt();
									}
								}
							}
							Log.w(TAG, "...found " + sessionStepsTaken + " steps for duration...");
							callback.resolve(sessionStepsTaken);
						})
						.addOnFailureListener(e -> {
							Log.w(TAG, "Unable to get recorded stats for duration", e);
							callback.reject();
						});
			});
		}
		return callback;
	}
}
