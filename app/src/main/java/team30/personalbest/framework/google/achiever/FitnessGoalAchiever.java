package team30.personalbest.framework.google.achiever;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.service.IGoalService;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.framework.watcher.OnFitnessUpdateListener;
import team30.personalbest.framework.snapshot.IFitnessSnapshot;

public class FitnessGoalAchiever implements OnFitnessUpdateListener
{
	public static final String TAG = "FitnessGoalAchiever";
	private final List<GoalListener> listeners = new ArrayList<>();
	private final IGoalService goalService;

	public FitnessGoalAchiever(IGoalService goalService)
	{
		this.goalService = goalService;
	}

	public FitnessGoalAchiever addGoalListener(GoalListener listener)
	{
		this.listeners.add(listener);
		return this;
	}

	public void removeGoalListener(GoalListener listener)
	{
		this.listeners.remove(listener);
	}

	private void achieveGoal()
	{
		for (GoalListener listener : this.listeners)
		{
			listener.onGoalAchievement(this.goalService);
		}
	}

	@Override
	public void onFitnessUpdate(IFitnessUser user, IFitnessClock clock, IFitnessSnapshot fitnessSnapshot)
	{
		if (fitnessSnapshot != null)
		{
			this.goalService.getGoalSnapshot(user, clock).onResult(iGoalSnapshot -> {
				if (iGoalSnapshot != null)
				{
					Log.d(TAG, "Trying to achieve goal " + iGoalSnapshot.getGoalValue() + " for " + fitnessSnapshot.getTotalSteps() + "...");
					if (fitnessSnapshot.getTotalSteps() >= iGoalSnapshot.getGoalValue())
					{
						FitnessGoalAchiever.this.achieveGoal();
					}
				}
			});
		}
	}
}
