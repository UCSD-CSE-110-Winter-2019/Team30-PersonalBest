package team30.personalbest.goal;

public interface GoalListener
{
    void onGoalAchievement(IGoalService goal);
    void onSubGoalAchievement(IGoalService goal);
}
