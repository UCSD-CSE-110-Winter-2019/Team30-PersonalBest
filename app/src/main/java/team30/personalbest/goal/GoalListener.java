package team30.personalbest.goal;

import team30.personalbest.service.goal.IGoalService;

public interface GoalListener
{
	void onGoalAchievement(IGoalService goal);

	void onSubGoalAchievement(IGoalService goal);
}
