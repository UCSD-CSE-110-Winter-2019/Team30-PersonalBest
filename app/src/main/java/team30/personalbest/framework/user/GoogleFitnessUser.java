package team30.personalbest.framework.user;

import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.google.EncouragementService;
import team30.personalbest.framework.google.FitnessService;
import team30.personalbest.framework.google.GoalService;
import team30.personalbest.framework.google.GoogleFitnessAdapter;
import team30.personalbest.framework.google.HeightService;
import team30.personalbest.framework.google.RecordingService;
import team30.personalbest.framework.snapshot.IFitnessSnapshot;
import team30.personalbest.framework.snapshot.IGoalSnapshot;
import team30.personalbest.util.Callback;

public class GoogleFitnessUser implements IFitnessUser
{
	private final GoogleFitnessAdapter googleFitnessAdapter;
	private HeightService heightService;
	private FitnessService fitnessService;
	private GoalService goalService;
	private RecordingService recordingService;
	private EncouragementService encouragementService;

	public GoogleFitnessUser(GoogleFitnessAdapter googleFitnessAdapter)
	{
		this.googleFitnessAdapter = googleFitnessAdapter;
		this.heightService = new HeightService();
		this.fitnessService = new FitnessService();
		this.goalService = new GoalService();
		this.recordingService = new RecordingService();
		this.encouragementService = new EncouragementService(this.fitnessService);

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

	public FitnessService getFitnessService()
	{
		return this.fitnessService;
	}

	public GoalService getGoalService()
	{
		return this.goalService;
	}

	public HeightService getHeightService()
	{
		return this.heightService;
	}

	public RecordingService getRecordingService()
	{
		return this.recordingService;
	}

	public EncouragementService getEncouragementService() { return this.encouragementService; }

	public GoogleFitnessAdapter getGoogleFitnessAdapter()
	{
		return this.googleFitnessAdapter;
	}
}
