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

	public static Callback<Bundle> buildBundleForDays(int numberOfDays, IFitnessUser fitnessUser, IFitnessClock clock)
	{
		final Callback<Bundle> callback = new Callback<>();
		{
			final long currentTime = clock.getCurrentTime();
			Calendar calendar = Calendar.getInstance();
			calendar.clear();
			calendar.setTimeInMillis(currentTime);
			calendar.add(Calendar.DAY_OF_YEAR, -numberOfDays);
			while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
			{
				calendar.add(Calendar.DATE, -1);
			}
			final long sundayTime = calendar.getTimeInMillis();

			final long minTime = Math.min(sundayTime, currentTime);
			final long maxTime = Math.max(sundayTime, currentTime);
			fitnessUser.getFitnessSnapshots(clock, minTime, maxTime)
					.onResult(iFitnessSnapshots -> {
						if (iFitnessSnapshots == null)
						{
							callback.resolve(null);
							return;
						}

						fitnessUser.getGoalSnapshots(clock, minTime, maxTime)
								.onResult(iGoalSnapshots -> {
									if (iGoalSnapshots == null)
									{
										callback.resolve(null);
										return;
									}

									final Bundle weeklyBundle = buildBundleForDays(numberOfDays, iFitnessSnapshots, iGoalSnapshots);
									weeklyBundle.putLong(GraphActivity.BUNDLE_WEEKLY_TIME, minTime);
									final Bundle bundle = new Bundle();
									bundle.putBundle(GraphActivity.BUNDLE_WEEKLY_STATS, weeklyBundle);
									callback.resolve(bundle);
								});
					});
		}
		return callback;
	}

	public static Callback<SnapshotObject> buildSnapshotObjectForDays(int numberOfDays, IFitnessUser fitnessUser, IFitnessClock clock)
	{
		final Callback<SnapshotObject> callback = new Callback<>();
		{
			final long currentTime = clock.getCurrentTime();
			Calendar calendar = Calendar.getInstance();
			calendar.clear();
			calendar.setTimeInMillis(currentTime);
			calendar.add(Calendar.DAY_OF_YEAR, -numberOfDays);
			while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
			{
				calendar.add(Calendar.DATE, -1);
			}
			final long sundayTime = calendar.getTimeInMillis();

			final long minTime = Math.min(sundayTime, currentTime);
			final long maxTime = Math.max(sundayTime, currentTime);
			fitnessUser.getFitnessSnapshots(clock, minTime, maxTime)
					.onResult(iFitnessSnapshots -> {
						if (iFitnessSnapshots == null)
						{
							callback.resolve(null);
							return;
						}

						fitnessUser.getGoalSnapshots(clock, minTime, maxTime)
								.onResult(iGoalSnapshots -> {
									if (iGoalSnapshots == null)
									{
										callback.resolve(null);
										return;
									}

									final SnapshotObject result = new SnapshotObject(iFitnessSnapshots, iGoalSnapshots);
									callback.resolve(result);
								});
					});
		}
		return callback;
	}

	public static Bundle buildBundleForDays(
			int numberOfDays,
			Iterable<IFitnessSnapshot> fitnessSnapshots,
			Iterable<IGoalSnapshot> goalSnapshots)
	{
		final Iterator<IFitnessSnapshot> fitnessIterator = fitnessSnapshots.iterator();
		final Iterator<IGoalSnapshot> stepGoalIterator = goalSnapshots.iterator();

		final Bundle result = new Bundle();

		int prevStepGoal = 0;
		int dayCount = 0;
		while (dayCount < numberOfDays)
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
