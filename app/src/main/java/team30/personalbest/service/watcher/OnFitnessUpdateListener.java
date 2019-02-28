package team30.personalbest.service.watcher;

import team30.personalbest.snapshot.IFitnessSnapshot;

public interface OnFitnessUpdateListener
{
	void onFitnessUpdate(IFitnessSnapshot fitnessSnapshot);
}
