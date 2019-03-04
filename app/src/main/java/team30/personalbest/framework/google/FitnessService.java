package team30.personalbest.framework.google;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
	public Callback<FitnessService> initialize(GoogleFitnessAdapter googleFitnessAdapter)
	{
		final Callback<FitnessService> callback = new Callback<>();
		{
			this.googleFitnessAdapter = googleFitnessAdapter;

			googleFitnessAdapter.getCurrentGoogleAccount().onResult(lastSignedInAccount -> {
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

				final long midnightTime = IFitnessClock.getMidnightOfDayTime(dayTime);
				final DataReadRequest readRequest = new DataReadRequest.Builder()
						.aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
						.bucketByTime(1, TimeUnit.DAYS)
						.setTimeRange(midnightTime, dayTime, TimeUnit.MILLISECONDS)
						.build();

				Fitness.getHistoryClient(activity, lastSignedInAccount)
						.readData(readRequest)
						.addOnSuccessListener(dataReadResponse -> {
							if (!dataReadResponse.getDataSets().isEmpty())
							{
								final DataSet dataSet = dataReadResponse.getDataSets().get(0);
								Log.d(TAG, "Retrieving daily steps..." + dataSet.toString());
								if (!dataSet.isEmpty())
								{
									final DataPoint dataPoint = dataSet.getDataPoints().get(0);
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
									Log.d(TAG, "..." + dataPoint + "...");
									callback.resolve(new FitnessSnapshot()
											                 .setTotalSteps(dataPoint.getValue(Field.FIELD_STEPS).asInt())
											                 .setStartTime(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
											                 .setStopTime(dataPoint.getEndTime(TimeUnit.MILLISECONDS)));
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
				if (lastSignedInAccount == null) { callback.reject(); return; }

				Log.d(TAG, "Getting multiple fitness data between " +
						startTime + " to " +
						stopTime + ", which is (" +
						(stopTime - startTime) + ")");

				final Activity activity = this.googleFitnessAdapter.getActivity();

				final DataReadRequest readRequest = new DataReadRequest.Builder()
						.aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
						.bucketByTime(1, TimeUnit.DAYS)
						.setTimeRange(startTime, stopTime + 1, TimeUnit.MILLISECONDS)
						.build();

				Fitness.getHistoryClient(activity, lastSignedInAccount)
						.readData(readRequest)
						.addOnSuccessListener(dataReadResponse -> {
							Log.d(TAG, "Retrieving multiple fitness data...");
							final List<IFitnessSnapshot> result = new ArrayList<>();
							final List<DataSet> dataSets = dataReadResponse.getDataSets();
							Log.d(TAG, "Data: " + dataSets.size());
							for (DataSet dataSet : dataSets)
							{
								Log.d(TAG, "..." + dataSet.toString());
								for (DataPoint data : dataSet.getDataPoints())
								{
									Log.d(TAG, "... ..." + data.toString());
									final FitnessSnapshot snapshot = new FitnessSnapshot()
											.setTotalSteps(data.getValue(Field.FIELD_STEPS).asInt())
											.setStartTime(data.getStartTime(TimeUnit.MILLISECONDS))
											.setStopTime(data.getEndTime(TimeUnit.MILLISECONDS))
											.setSpeed(data.getValue(Field.FIELD_SPEED).asFloat());
									result.add(snapshot);
								}
							}
							callback.resolve(result);
						})
						.addOnFailureListener(e -> {
							Log.w(TAG, "Failed to retrieve fitness data for range.", e);
							callback.reject();
						});

				final SessionReadRequest sessionReadRequest = new SessionReadRequest.Builder()
						.read(DataType.TYPE_STEP_COUNT_CUMULATIVE)
						.read(DataType.TYPE_SPEED)
						.setSessionName(GoogleFitnessAdapter.RECORDING_SESSION_NAME)
						.setTimeInterval(startTime, stopTime + 1, TimeUnit.MILLISECONDS)
						.build();

				/*
				Fitness.getSessionsClient(activity, lastSignedInAccount)
						.readSession(sessionReadRequest)
						.addOnSuccessListener(sessionReadResponse -> {
							List<IFitnessSnapshot> snapshots = new ArrayList<>();

							final List<Session> sessions = sessionReadResponse.getSessions();
							for (Session session : sessions)
							{
								final List<DataSet> dataSets = sessionReadResponse.getDataSet(session);
								for (DataSet dataSet : dataSets)
								{
									for (DataPoint data : dataSet.getDataPoints())
									{
										FitnessSnapshot snapshot = new FitnessSnapshot();

										snapshot.setStartTime(data.getStartTime(TimeUnit.MILLISECONDS));
										snapshot.setStopTime(data.getEndTime(TimeUnit.MILLISECONDS));

										snapshot.setTotalSteps(
												data.getValue(Field.FIELD_STEPS).asInt());
										//snapshot.setSpeed(data.getValue(Field.FIELD_DISTANCE).asFloat());

										snapshots.add(snapshot);
									}
								}
							}

							callback.resolve(snapshots);
						})
						.addOnFailureListener(e -> {
							Log.w(TAG, "Unable to get step count for duration", e);
							callback.reject();
						});
						*/
			});
		}
		return callback;
	}
}
