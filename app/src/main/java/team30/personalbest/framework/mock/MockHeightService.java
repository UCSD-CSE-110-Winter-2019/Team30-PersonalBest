package team30.personalbest.framework.mock;

import team30.personalbest.framework.IFitnessAdapter;
import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.google.GoogleFitnessAdapter;
import team30.personalbest.framework.google.HeightService;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.util.Callback;

public class MockHeightService extends HeightService
{
	@Override
	public Callback<HeightService> initialize(IFitnessAdapter googleFitnessAdapter)
	{
		return new Callback<>(this);
	}

	@Override
	public Callback<Float> getHeight(IFitnessUser user, IFitnessClock clock)
	{
		return new Callback<>(1.0F);
	}

	@Override
	public Callback<Float> setHeight(IFitnessUser user, IFitnessClock clock, float height)
	{
		return new Callback<>(1.0F);
	}
}
