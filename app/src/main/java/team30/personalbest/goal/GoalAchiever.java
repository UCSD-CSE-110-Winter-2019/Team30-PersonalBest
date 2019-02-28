package team30.personalbest.goal;

import team30.personalbest.service.goal.IGoalService;

public interface GoalAchiever
{
	void startAchievingGoal();

	void stopAchievingGoal();

	void doAchieveGoal();

	void doAchieveSubGoal();

	GoalAchiever addGoalListener(GoalListener listener);

	void removeGoalListener(GoalListener listener);

	void clearGoalListeners();

	Iterable<GoalListener> getGoalListeners();

	IGoalService getStepGoal();

	GoalAchiever setStepGoal(IGoalService goal);
}
