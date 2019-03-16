package team30.personalbest.framework.service;

import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.util.Callback;

public interface IHeightService
{
	Callback<Float> getHeight(IFitnessUser user, IFitnessClock clock);
}
