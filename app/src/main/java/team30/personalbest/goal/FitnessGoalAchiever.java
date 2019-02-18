package team30.personalbest.goal;

import android.support.v4.util.Consumer;

import java.util.ArrayList;
import java.util.List;

import team30.personalbest.service.goal.IGoalService;
import team30.personalbest.service.watcher.OnFitnessUpdateListener;
import team30.personalbest.snapshot.IFitnessSnapshot;
import team30.personalbest.snapshot.IGoalSnapshot;

public class FitnessGoalAchiever implements OnFitnessUpdateListener
{
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
	public void onFitnessUpdate(final IFitnessSnapshot fitnessSnapshot)
	{
		if (fitnessSnapshot != null)
		{
			this.goalService.getGoalSnapshot().onResult(new Consumer<IGoalSnapshot>() {
				@Override
				public void accept(IGoalSnapshot iGoalSnapshot)
				{

					if (fitnessSnapshot.getTotalSteps() >= iGoalSnapshot.getGoalValue())
					{
						FitnessGoalAchiever.this.achieveGoal();
					}
				}
			});
		}
	}
}
