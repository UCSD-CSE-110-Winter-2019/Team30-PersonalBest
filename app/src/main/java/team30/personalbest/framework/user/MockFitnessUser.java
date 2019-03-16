package team30.personalbest.framework.user;

import team30.personalbest.framework.IFitnessAdapter;
import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.google.EncouragementService;
import team30.personalbest.framework.google.FitnessService;
import team30.personalbest.framework.google.GoalService;
import team30.personalbest.framework.google.HeightService;
import team30.personalbest.framework.google.RecordingService;
import team30.personalbest.framework.mock.MockEncouragementService;
import team30.personalbest.framework.mock.MockFitnessService;
import team30.personalbest.framework.mock.MockGoalService;
import team30.personalbest.framework.mock.MockHeightService;
import team30.personalbest.framework.mock.MockRecordingService;
import team30.personalbest.framework.snapshot.IFitnessSnapshot;
import team30.personalbest.framework.snapshot.IGoalSnapshot;
import team30.personalbest.util.Callback;

public class MockFitnessUser implements IGoogleFitnessUser
{
	private final IFitnessAdapter googleFitnessAdapter;
	private HeightService heightService;
	private FitnessService fitnessService;
	private GoalService goalService;
	private EncouragementService encouragementService;
	private RecordingService recordingService;

	public MockFitnessUser(IFitnessAdapter googleFitnessAdapter)
	{
		this.googleFitnessAdapter = googleFitnessAdapter;
		this.heightService = new MockHeightService();
		this.fitnessService = new MockFitnessService();
		this.goalService = new MockGoalService();
		this.recordingService = new MockRecordingService();
		this.encouragementService = new MockEncouragementService(this.fitnessService);

		googleFitnessAdapter
				.addGoogleService(this.fitnessService)
				.addGoogleService(this.goalService)
				.addGoogleService(this.heightService)
				.addGoogleService(this.recordingService)
				.addGoogleService(this.encouragementService);
	}

	@Override
	public Callback<Integer> getCurrentDailySteps(IFitnessClock clock)
	{
		return this.fitnessService.getDailySteps(this, clock, clock.getCurrentTime());
	}

	@Override
	public Callback<IFitnessSnapshot> getCurrentFitnessSnapshot(IFitnessClock clock)
	{
		return this.fitnessService.getFitnessSnapshot(this, clock);
	}

	@Override
	public Callback<Iterable<IFitnessSnapshot>> getFitnessSnapshots(IFitnessClock clock, long startTime, long stopTime)
	{
		return this.fitnessService.getFitnessSnapshots(this, clock, startTime, stopTime);
	}

	@Override
	public Callback<IGoalSnapshot> getCurrentGoalSnapshot(IFitnessClock clock)
	{
		return this.goalService.getGoalSnapshot(this, clock);
	}

	@Override
	public Callback<Iterable<IGoalSnapshot>> getGoalSnapshots(IFitnessClock clock, long startTime, long stopTime)
	{
		return this.goalService.getGoalSnapshots(this, clock, startTime, stopTime);
	}

	@Override
	public Callback<Float> getHeight(IFitnessClock clock)
	{
		return this.heightService.getHeight(this, clock);
	}

	@Override
	public FitnessService getFitnessService()
	{
		return this.fitnessService;
	}

	@Override
	public GoalService getGoalService()
	{
		return this.goalService;
	}

	@Override
	public HeightService getHeightService()
	{
		return this.heightService;
	}

	@Override
	public RecordingService getRecordingService()
	{
		return this.recordingService;
	}

	@Override
	public EncouragementService getEncouragementService()
	{
		return this.encouragementService;
	}

	@Override
	public IFitnessAdapter getGoogleFitnessAdapter()
	{
		return this.googleFitnessAdapter;
	}
}
