package team30.personalbest.framework.achiever;

import team30.personalbest.framework.service.IGoalService;

public interface GoalListener {
    void onGoalAchievement(IGoalService goal);
}
