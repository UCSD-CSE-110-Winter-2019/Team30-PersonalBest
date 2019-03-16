package team30.personalbest.framework.google;

import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.SessionInsertRequest;

import java.util.concurrent.TimeUnit;

import team30.personalbest.framework.IFitnessAdapter;
import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.google.recorder.GoogleFitDataRecorder;
import team30.personalbest.framework.google.recorder.RecordingSnapshot;
import team30.personalbest.framework.service.IRecordingService;
import team30.personalbest.framework.snapshot.IFitnessSnapshot;
import team30.personalbest.framework.snapshot.IRecordingFitnessSnapshot;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.util.Callback;

public class RecordingService implements IRecordingService, IGoogleService
{
	public static final String TAG = "RecordingService";

	private boolean recording = false;
	private RecordingSnapshot recordingSnapshot;

	private GoogleFitDataRecorder stepRecorder;

	private GoogleFitnessAdapter googleFitnessAdapter;

	@Override
	public Callback<RecordingService> initialize(IFitnessAdapter googleFitnessAdapter)
	{
		final Callback<RecordingService> callback = new Callback<>();
		{
			this.googleFitnessAdapter = (GoogleFitnessAdapter) googleFitnessAdapter;

			this.stepRecorder = new GoogleFitDataRecorder(this.googleFitnessAdapter, DataType.TYPE_STEP_COUNT_DELTA, GoogleFitnessAdapter.RECORDER_SAMPLING_RATE);

			callback.resolve(this);
		}
		return callback;
	}

	@Override
	public IRecordingFitnessSnapshot startRecording(IFitnessUser user, IFitnessClock clock)
	{
		if (this.recording)
			throw new IllegalStateException("Already started recording");
		this.recording = true;

		this.recordingSnapshot = new RecordingSnapshot(clock.getCurrentTime());
		this.stepRecorder.start(clock, this.recordingSnapshot);

		return this.recordingSnapshot;
	}

	@Override
	public Callback<IFitnessSnapshot> stopRecording(IFitnessUser user, IFitnessClock clock)
	{
		if (!this.recording)
			throw new IllegalStateException("Must call startRecording first");
		this.recording = false;

		final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this.googleFitnessAdapter.getActivity());
		if (lastSignedInAccount != null)
		{
			DataSet stepData = this.stepRecorder.stop();

			if (stepData != null && !stepData.isEmpty())
			{
				final Session session = new Session.Builder()
						.setName(GoogleFitnessAdapter.RECORDING_SESSION_NAME)
						.setDescription(GoogleFitnessAdapter.RECORDING_SESSION_DESCRIPTION)
						.setIdentifier(GoogleFitnessAdapter.RECORDING_SESSION_ID)
						.setActivity(FitnessActivities.RUNNING)
						.setStartTime(this.recordingSnapshot.getStartTime(), TimeUnit.MILLISECONDS)
						.setEndTime(this.recordingSnapshot.getStopTime(), TimeUnit.MILLISECONDS)
						.build();
				final SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
						.setSession(session)
						.addDataSet(stepData)
						.build();

				Fitness.getSessionsClient(this.googleFitnessAdapter.getActivity(), lastSignedInAccount)
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
	public IRecordingFitnessSnapshot getRecordingSnapshot(IFitnessUser user)
	{
		if (!this.recording)
			throw new IllegalStateException("Cannot get inactive recording snapshot");
		return this.recordingSnapshot;
	}

	@Override
	public boolean isRecording()
	{
		return this.recording;
	}
}
