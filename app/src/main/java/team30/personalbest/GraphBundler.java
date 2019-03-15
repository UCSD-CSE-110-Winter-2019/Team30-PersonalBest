package team30.personalbest;

import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;
import java.util.Iterator;

import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.snapshot.IFitnessSnapshot;
import team30.personalbest.framework.snapshot.IGoalSnapshot;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.util.Callback;

public class GraphBundler
{
	public static final String TAG = "GraphBundler";

	private GraphBundler() {}

	public static Callback<Bundle> makeBundle(IFitnessClock clock, IFitnessUser user)
	{
		final Callback<Bundle> callback = new Callback<>();
		{
			final long currentTime = clock.getCurrentTime();
			Calendar calendar = Calendar.getInstance();
			calendar.clear();
			calendar.setTimeInMillis(currentTime);
			while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
			{
				calendar.add(Calendar.DATE, -1);
			}
			final long sundayTime = calendar.getTimeInMillis();

			final long minTime = Math.min(sundayTime, currentTime);
			final long maxTime = Math.max(sundayTime, currentTime);
			user.getFitnessSnapshots(clock, minTime, maxTime)
					.onResult(iFitnessSnapshots -> {
						if (iFitnessSnapshots == null)
						{
							callback.resolve(null);
							return;
						}

						user.getGoalSnapshots(clock, minTime, maxTime)
								.onResult(iGoalSnapshots -> {
									if (iGoalSnapshots == null)
									{
										callback.resolve(null);
										return;
									}

									final Bundle weeklyBundle = buildWeeklyBundle(iFitnessSnapshots, iGoalSnapshots);
									weeklyBundle.putLong(GraphActivity.BUNDLE_WEEKLY_TIME, minTime);
									final Bundle bundle = new Bundle();
									bundle.putBundle(GraphActivity.BUNDLE_WEEKLY_STATS, weeklyBundle);
									callback.resolve(bundle);
								});
					});
		}
		return callback;
	}

	private static Bundle buildWeeklyBundle(
			Iterable<IFitnessSnapshot> fitnessSnapshots,
			Iterable<IGoalSnapshot> stepGoals)
	{
		final Iterator<IFitnessSnapshot> fitnessIterator = fitnessSnapshots.iterator();
		final Iterator<IGoalSnapshot> stepGoalIterator = stepGoals.iterator();

		final Bundle result = new Bundle();

		int prevStepGoal = 0;
		int dayCount = 0;
		while (dayCount < GraphActivity.BUNDLE_WEEK_LENGTH)
		{
			final Bundle dailyBundle = new Bundle();

			if (fitnessIterator.hasNext())
			{
				IFitnessSnapshot snapshot = fitnessIterator.next();
				dailyBundle.putInt(GraphActivity.BUNDLE_DAILY_STEPS,
				                   snapshot.getTotalSteps());
				dailyBundle.putInt(GraphActivity.BUNDLE_DAILY_ACTIVE_STEPS,
				                   snapshot.getRecordedSteps());
				dailyBundle.putLong(GraphActivity.BUNDLE_DAILY_TIMES,
				                    snapshot.getStopTime() - snapshot.getStartTime());
				dailyBundle.putDouble(GraphActivity.BUNDLE_DAILY_MPH,
				                      snapshot.getSpeed());
			}

			if (stepGoalIterator.hasNext())
			{
				IGoalSnapshot snapshot = stepGoalIterator.next();
				int stepGoal = snapshot.getGoalValue();
				if (stepGoal >= Integer.MAX_VALUE)
				{
					dailyBundle.putInt(GraphActivity.BUNDLE_DAILY_GOALS, prevStepGoal);
				}
				else
				{
					dailyBundle.putInt(GraphActivity.BUNDLE_DAILY_GOALS, stepGoal);
					prevStepGoal = stepGoal;
				}
			}

			Log.d(TAG, "Day " + dayCount + ": " + dailyBundle.toString());

			//Insert into result
			result.putBundle(GraphActivity.BUNDLE_WEEKLY_PREFIX + dayCount, dailyBundle);
			++dayCount;
		}

		return result;
	}
}
