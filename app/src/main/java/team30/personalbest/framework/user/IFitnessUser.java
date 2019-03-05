package team30.personalbest.framework.user;

import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.snapshot.IFitnessSnapshot;
import team30.personalbest.framework.snapshot.IGoalSnapshot;
import team30.personalbest.util.Callback;

public interface IFitnessUser
{
	Callback<Integer> getCurrentDailySteps(IFitnessClock clock);

	Callback<IFitnessSnapshot> getCurrentFitnessSnapshot(IFitnessClock clock);
	Callback<Iterable<IFitnessSnapshot>> getFitnessSnapshots(IFitnessClock clock, long startTime, long stopTime);

	Callback<IGoalSnapshot> getCurrentGoalSnapshot(IFitnessClock clock);
	Callback<Iterable<IGoalSnapshot>> getGoalSnapshots(IFitnessClock clock, long startTime, long stopTime);

	Callback<Float> getHeight(IFitnessClock clock);
}
