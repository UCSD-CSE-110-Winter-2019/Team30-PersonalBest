package team30.personalbest.service.goal;

import team30.personalbest.service.fitness.IFitnessService;
import team30.personalbest.snapshot.IGoalSnapshot;
import team30.personalbest.service.IService;
import team30.personalbest.util.Callback;

public interface IGoalService extends IService
{
	Callback<IGoalSnapshot> isCurrentGoalAchieved(IFitnessService fitnessService);

	Callback<IGoalSnapshot> setCurrentGoal(int goalValue);

	Callback<IGoalSnapshot> getGoalSnapshot();

	Callback<Iterable<IGoalSnapshot>> getGoalSnapshots(long startTime, long stopTime);
}
