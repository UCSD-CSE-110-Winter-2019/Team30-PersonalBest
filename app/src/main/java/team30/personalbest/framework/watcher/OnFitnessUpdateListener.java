package team30.personalbest.framework.watcher;

import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.framework.snapshot.IFitnessSnapshot;

public interface OnFitnessUpdateListener
{
	void onFitnessUpdate(IFitnessUser user, IFitnessClock clock, IFitnessSnapshot fitnessSnapshot);
}
