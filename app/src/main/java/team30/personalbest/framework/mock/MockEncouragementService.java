package team30.personalbest.framework.mock;

import android.content.Context;

import team30.personalbest.framework.IFitnessAdapter;
import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.google.EncouragementService;
import team30.personalbest.framework.service.IFitnessService;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.util.Callback;

public class MockEncouragementService extends EncouragementService
{
	public MockEncouragementService(IFitnessService fitnessService)
	{
		super(fitnessService);
	}

	@Override
	public Callback<EncouragementService> initialize(IFitnessAdapter googleFitnessAdapter)
	{
		return new Callback<>(this);
	}

	@Override
	public long getLastEncouragementTime(Context context)
	{
		return 0;
	}

	@Override
	public void tryEncouragement(Context context, IFitnessUser user, IFitnessClock clock, long currentTime)
	{
		return;
	}
}
