package team30.personalbest.framework.service;

import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.snapshot.IGoalSnapshot;
import team30.personalbest.util.Callback;

public interface IGoalService
{
	Callback<IGoalSnapshot> getGoalSnapshot(IFitnessUser user, IFitnessClock clock);
	Callback<Iterable<IGoalSnapshot>> getGoalSnapshots(IFitnessUser user, IFitnessClock clock, long startTime, long stopTime);
}
