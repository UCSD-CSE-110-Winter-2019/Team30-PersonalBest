package team30.personalbest.framework.mock;

import team30.personalbest.framework.IFitnessAdapter;
import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.google.RecordingService;
import team30.personalbest.framework.snapshot.FitnessSnapshot;
import team30.personalbest.framework.snapshot.IFitnessSnapshot;
import team30.personalbest.framework.snapshot.IRecordingFitnessSnapshot;
import team30.personalbest.framework.snapshot.OnRecordingSnapshotUpdateListener;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.util.Callback;

public class MockRecordingService extends RecordingService
{
	private final IRecordingFitnessSnapshot snapshot = new IRecordingFitnessSnapshot() {
		@Override
		public IRecordingFitnessSnapshot addOnRecordingSnapshotUpdateListener(OnRecordingSnapshotUpdateListener listener)
		{
			return this;
		}

		@Override
		public int getRecordedSteps()
		{
			return 0;
		}

		@Override
		public int getTotalSteps()
		{
			return 0;
		}

		@Override
		public long getStartTime()
		{
			return 0;
		}

		@Override
		public long getStopTime()
		{
			return 0;
		}

		@Override
		public double getSpeed()
		{
			return 0;
		}
	};

	private boolean recording = false;

	@Override
	public Callback<RecordingService> initialize(IFitnessAdapter googleFitnessAdapter)
	{
		return new Callback<>(this);
	}

	@Override
	public IRecordingFitnessSnapshot startRecording(IFitnessUser user, IFitnessClock clock)
	{
		this.recording = true;
		return this.snapshot;
	}

	@Override
	public Callback<IFitnessSnapshot> stopRecording(IFitnessUser user, IFitnessClock clock)
	{
		this.recording = false;
		final FitnessSnapshot result = new FitnessSnapshot();
		result.setStartTime(clock.getCurrentTime());
		result.setStopTime(clock.getCurrentTime() + 1);
		return new Callback<>(result);
	}

	@Override
	public IRecordingFitnessSnapshot getRecordingSnapshot(IFitnessUser user)
	{
		return this.snapshot;
	}

	@Override
	public boolean isRecording()
	{
		return this.recording;
	}
}
