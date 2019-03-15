package team30.personalbest.service.fitness;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Consumer;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import team30.personalbest.MainActivity;
import team30.personalbest.service.recorder.GoogleFitDataRecorder;
import team30.personalbest.snapshot.FitnessSnapshot;
import team30.personalbest.snapshot.IFitnessSnapshot;
import team30.personalbest.snapshot.IRecordingFitnessSnapshot;
import team30.personalbest.snapshot.RecordingSnapshot;
import team30.personalbest.service.ServiceInitializer;
import team30.personalbest.service.IService;
import team30.personalbest.service.recorder.IRecorderService;
import team30.personalbest.util.Callback;

public class GoogleFitAdapter implements IFitnessService, IRecorderService, Serializable
{
	public static final String TAG = "GoogleFitAdapter";

	public static final String RECORDING_SESSION_ID = "PersonalBestRun";
	public static final String RECORDING_SESSION_NAME = "Personal Best Run";
	public static final String RECORDING_SESSION_DESCRIPTION = "Doing a run";
	public static final int RECORDER_SAMPLING_RATE = 1;

	private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;

	private boolean recording = false;
	private transient GoogleFitDataRecorder stepRecorder;
	private transient RecordingSnapshot recordingSnapshot;

	private transient Activity activity;
	private long customTime = -1;

	public GoogleFitAdapter setCurrentTime(long millis)
	{
		this.customTime = millis;
		return this;
	}

	@Override
	public IRecordingFitnessSnapshot startRecording()
	{
		if (this.recording)
			throw new IllegalStateException("Already started recording");
		this.recording = true;

		this.recordingSnapshot = new RecordingSnapshot(this);

		this.stepRecorder = new GoogleFitDataRecorder(this.activity,
		                                              DataType.TYPE_STEP_COUNT_CUMULATIVE,
		                                              RECORDER_SAMPLING_RATE).setHandler(this.recordingSnapshot);
		this.stepRecorder.start();

		return this.recordingSnapshot;
	}

	@Override
	public Callback<IFitnessSnapshot> stopRecording()
	{
		if (!this.recording)
			throw new IllegalStateException("Must call startRecording first");
		this.recording = false;

		final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this.activity);
		if (lastSignedInAccount != null)
		{
			DataSet stepData = this.stepRecorder.stop();

			if (stepData != null && !stepData.isEmpty())
			{
				final Session session = new Session.Builder()
						.setName(RECORDING_SESSION_NAME)
						.setDescription(RECORDING_SESSION_DESCRIPTION)
						.setIdentifier(RECORDING_SESSION_ID)
						.setActivity(FitnessActivities.RUNNING)
						.setStartTime(this.recordingSnapshot.getStartTime(), TimeUnit.MILLISECONDS)
						.setEndTime(this.recordingSnapshot.getStopTime(), TimeUnit.MILLISECONDS)
						.build();
				final SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
						.setSession(session)
						.addDataSet(stepData)
						.build();

				Fitness.getSessionsClient(this.activity, lastSignedInAccount)
						.insertSession(insertRequest)
						.addOnSuccessListener(aVoid -> Log.i(TAG, "Successfully inserted session data."))
						.addOnFailureListener(e -> Log.w(TAG, "Failed to insert session data.", e));
			}
			else
			{
				Log.w(TAG, "Found no fitness data to insert.");
			}
		}

		final RecordingSnapshot result = this.recordingSnapshot;
		this.recordingSnapshot = null;
		return new Callback<>(result);
	}

	@Override
	public boolean isRecording()
	{
		return this.recording;
	}

	@Override
	public IRecordingFitnessSnapshot getRecordingSnapshot()
	{
		if (!this.recording)
			throw new IllegalStateException("Cannot get inactive recording snapshot");
		return this.recordingSnapshot;
	}

	@Override
	public Callback<Iterable<IFitnessSnapshot>> getRecordingSnapshots(long startTime, long stopTime)
	{
		final Callback<Iterable<IFitnessSnapshot>> callback = new Callback<>();
		{
			final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this.activity);
			if (lastSignedInAccount == null)
			{
				callback.resolve(null);
			}
			else
			{
				Log.d(TAG, "Getting multiple recording data between " + startTime +
						" to " + stopTime + ", which is (" + (stopTime - startTime) + ")");

				SessionReadRequest readRequest = new SessionReadRequest.Builder()
						.setTimeInterval(startTime, stopTime, TimeUnit.MILLISECONDS)
						.read(DataType.TYPE_STEP_COUNT_CUMULATIVE)
						.read(DataType.TYPE_DISTANCE_CUMULATIVE)
						.setSessionName(RECORDING_SESSION_NAME)
						.build();

				Fitness.getSessionsClient(this.activity, lastSignedInAccount)
						.readSession(readRequest)
						.addOnSuccessListener(new OnSuccessListener<SessionReadResponse>()
						{
							@Override
							public void onSuccess(SessionReadResponse sessionReadResponse)
							{
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
                                            /*snapshot.setSpeed(
                                                    data.getValue(Field.FIELD_DISTANCE).asFloat());*/

											snapshots.add(snapshot);
										}
									}
								}

								callback.resolve(snapshots);
							}
						})
						.addOnFailureListener(new OnFailureListener()
						{
							@Override
							public void onFailure(@NonNull Exception e)
							{
								Log.w(TAG, "Unable to get step count for duration", e);

								callback.resolve(null);
							}
						});
			}
		}
		return callback;
	}

	@Override
	public Callback<Iterable<IFitnessSnapshot>> getFitnessSnapshots(long startTime, long stopTime)
	{
		final Callback<Iterable<IFitnessSnapshot>> callback = new Callback<>();
		{
			final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this.activity);
			if (lastSignedInAccount == null)
			{
				callback.resolve(null);
			}
			else
			{
				Log.d(TAG, "Getting multiple fitness data between " + startTime +
						" to " + stopTime + ", which is (" + (stopTime - startTime) + ")");

				final DataReadRequest readRequest = new DataReadRequest.Builder()
						.aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
						.bucketByTime(1, TimeUnit.DAYS)
						.setTimeRange(startTime, stopTime + 1, TimeUnit.MILLISECONDS)
						.build();
				Fitness.getHistoryClient(this.activity, lastSignedInAccount)
						.readData(readRequest)
						.addOnSuccessListener(new OnSuccessListener<DataReadResponse>()
						{
							@Override
							public void onSuccess(DataReadResponse dataReadResponse)
							{
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
							}
						})
						.addOnFailureListener(new OnFailureListener()
						{
							@Override
							public void onFailure(@NonNull Exception e)
							{
								Log.w(TAG, "Failed to retrieve fitness data for range.", e);
							}
						});
			}
		}
		return callback;
	}

	@Override
	public Callback<IFitnessSnapshot> getFitnessSnapshot()
	{
		final Callback<IFitnessSnapshot> callback = new Callback<>();
		{
			final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this.activity);
			if (lastSignedInAccount == null)
			{
				callback.resolve(null);
			}
			else
			{
				Fitness.getHistoryClient(this.activity, lastSignedInAccount)
						.readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
						.addOnSuccessListener(new OnSuccessListener<DataSet>()
						{
							@Override
							public void onSuccess(DataSet dataSet)
							{
								Log.d(TAG, "Retrieving fitness data..." + dataSet.toString());

								FitnessSnapshot result = null;
								if (!dataSet.isEmpty())
								{
									final DataPoint dataPoint = dataSet.getDataPoints().get(0);
									result = new FitnessSnapshot()
											.setTotalSteps(dataPoint.getValue(Field.FIELD_STEPS).asInt())
											.setStartTime(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
											.setStopTime(dataPoint.getEndTime(TimeUnit.MILLISECONDS));
									//.setSpeed(dataPoint.getValue(Field.FIELD_SPEED).asFloat());
								}

								callback.resolve(result);
							}
						})
						.addOnFailureListener(new OnFailureListener()
						{
							@Override
							public void onFailure(@NonNull Exception e)
							{
								Log.w(TAG, "Failed to retrieve fitness data.", e);
							}
						});
			}
                /*
                final long stopTime = this.getCurrentTime();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(stopTime);
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                final long startTime = calendar.getTimeInMillis();

                final DataReadRequest readRequest = new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .aggregate(DataType.TYPE_SPEED, DataType.AGGREGATE_SPEED_SUMMARY)
                        .bucketByTime(1, TimeUnit.DAYS)
                        .setTimeRange(startTime, stopTime, TimeUnit.MILLISECONDS)
                        .build();
                Fitness.getHistoryClient(this.activity, lastSignedInAccount)
                        .readData(readRequest)
                        .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                if (dataReadResponse.getDataSets().isEmpty())
                                {
                                    Log.w(TAG, "Could not find recent fitness data");
                                    callback.resolve(null);
                                }
                                else
                                {
                                    DataSet dataSet = dataReadResponse.getDataSets().get(0);
                                    Log.d(TAG, "Retrieving fitness data..." + dataSet.toString());

                                    FitnessSnapshot result = null;
                                    if (!dataSet.isEmpty())
                                    {
                                        final DataPoint dataPoint = dataSet.getDataPoints().get(0);
                                        result = new FitnessSnapshot()
                                                .setTotalSteps(dataPoint.getValue(Field.FIELD_STEPS).asInt())
                                                .setStartTime(dataPoint.getStartTime(TimeUnit.MILLISECONDS))
                                                .setStopTime(dataPoint.getEndTime(TimeUnit.MILLISECONDS))
                                                .setSpeed(dataPoint.getValue(Field.FIELD_SPEED).asFloat());
                                    }

                                    callback.resolve(result);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Failed to retrieve fitness data.", e);
                            }
                        });
            }
            */
		}
		return callback;
	}

	@Override
	public long getCurrentTime()
	{
		if (this.customTime >= 0)
		{
			return this.customTime;
		}
		else
		{
			return System.currentTimeMillis();
		}
	}

	@Nullable
	@Override
	public Callback<IService> onActivityCreate(ServiceInitializer serviceInitializer, Activity activity, Bundle savedInstanceState)
	{
		this.activity = activity;

		final FitnessOptions fitnessOptions = FitnessOptions.builder()
				.addDataType(DataType.TYPE_STEP_COUNT_DELTA)
				.addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA)
				.addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_WRITE)
				.addDataType(DataType.TYPE_DISTANCE_DELTA)
				.addDataType(DataType.AGGREGATE_DISTANCE_DELTA)
				.addDataType(DataType.TYPE_SPEED)
				.addDataType(DataType.AGGREGATE_SPEED_SUMMARY)
				.addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_WRITE)
				.build();

		if (!GoogleSignIn.hasPermissions(
				GoogleSignIn.getLastSignedInAccount(activity),
				fitnessOptions))
		{
			GoogleSignIn.requestPermissions(
					activity,
					REQUEST_OAUTH_REQUEST_CODE,
					GoogleSignIn.getLastSignedInAccount(activity),
					fitnessOptions
			);

			return null;
		}
		else
		{
			return this.subscribeToGoogleServices(activity);
		}
	}

	@Nullable
	@Override
	public Callback<IService> onActivityResult(ServiceInitializer serviceInitializer, final Activity activity, int requestCode, int resultCode, @Nullable Intent data)
	{
		this.activity = activity;

		if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_OAUTH_REQUEST_CODE)
		{
			this.subscribeToGoogleServices(activity).onResult(new Consumer<IService>() {
				@Override
				public void accept(IService iService)
				{
					activity.recreate();
				}
			});
			return null;
		}
		else
		{
			return null;
		}
	}

	private Callback<IService> subscribeToGoogleServices(Activity activity)
	{
		final Callback<IService> callback = new Callback<>();
		{
			final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity);
			if (lastSignedInAccount == null)
			{
				Log.w(TAG, "Could not find google account");
				callback.resolve(null);
			}
			else
			{
				//Record steps passively...
				Fitness.getRecordingClient(activity, lastSignedInAccount)
						.subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
						.addOnSuccessListener(new OnSuccessListener<Void>()
						{
							@Override
							public void onSuccess(Void aVoid)
							{
								Log.i(TAG, "Successfully subscribed google recording services for passive counting.");
							}
						})
						.addOnFailureListener(new OnFailureListener()
						{
							@Override
							public void onFailure(@NonNull Exception e)
							{
								Log.w(TAG, "Failed to subscribe to google recording services for passive counting!", e);
							}
						});

				//I am ready!
				callback.resolve(this);
			}
		}
		return callback;
	}
}
