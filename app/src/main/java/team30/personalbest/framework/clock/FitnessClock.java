package team30.personalbest.framework.clock;

public class FitnessClock implements IFitnessClock
{
	private long frozenTime = -1;

	public void freezeTimeAt(long time)
	{
		this.frozenTime = time;
	}

	public void unfreeze()
	{
		this.frozenTime = -1;
	}

	@Override
	public long getCurrentTime()
	{
		if (this.frozenTime >= 0)
		{
			return this.frozenTime;
		}
		else
		{
			return System.currentTimeMillis();
		}
	}
}
