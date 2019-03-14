package team30.personalbest.framework.service;

import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.framework.snapshot.IFitnessSnapshot;
import team30.personalbest.util.Callback;

public interface IFitnessService
{
	Callback<Integer> getDailySteps(IFitnessUser user, IFitnessClock clock, long dayTime);
	Callback<IFitnessSnapshot> getFitnessSnapshot(IFitnessUser user, IFitnessClock clock);
	Callback<Iterable<IFitnessSnapshot>> getFitnessSnapshots(IFitnessUser user, IFitnessClock clock, long startTime, long stopTime);
}
