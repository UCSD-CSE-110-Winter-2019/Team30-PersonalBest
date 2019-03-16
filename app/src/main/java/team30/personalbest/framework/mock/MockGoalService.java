package team30.personalbest.framework.mock;

import java.util.ArrayList;

import team30.personalbest.framework.IFitnessAdapter;
import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.google.GoalService;
import team30.personalbest.framework.snapshot.GoalSnapshot;
import team30.personalbest.framework.snapshot.IGoalSnapshot;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.util.Callback;

public class MockGoalService extends GoalService
{
	@Override
	public Callback<GoalService> initialize(IFitnessAdapter googleFitnessAdapter)
	{
		return new Callback<>(this);
	}

	@Override
	public Callback<IGoalSnapshot> getGoalSnapshot(IFitnessUser user, IFitnessClock clock)
	{
		return new Callback<>(new GoalSnapshot());
	}

	@Override
	public Callback<Iterable<IGoalSnapshot>> getGoalSnapshots(IFitnessUser user, IFitnessClock clock, long startTime, long stopTime)
	{
		return new Callback<>(new ArrayList<>());
	}

	@Override
	public Callback<IGoalSnapshot> setCurrentGoal(IFitnessUser user, IFitnessClock clock, int goalValue)
	{
		return new Callback<>(new GoalSnapshot());
	}
}
