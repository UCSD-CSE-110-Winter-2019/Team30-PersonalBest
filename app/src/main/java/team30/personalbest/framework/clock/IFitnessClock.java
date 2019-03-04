package team30.personalbest.framework.clock;

import java.util.Calendar;

public interface IFitnessClock
{
	static long getMidnightOfDayTime(long time)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTimeInMillis();
	}

	long getCurrentTime();
}
