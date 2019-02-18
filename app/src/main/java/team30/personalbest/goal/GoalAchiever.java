package team30.personalbest.goal;

public interface GoalAchiever
{
    GoalAchiever setStepGoal(IGoalService goal);

    void startAchievingGoal();
    void stopAchievingGoal();

    void doAchieveGoal();
    void doAchieveSubGoal();

    GoalAchiever addGoalListener(GoalListener listener);
    void removeGoalListener(GoalListener listener);
    void clearGoalListeners();
    Iterable<GoalListener> getGoalListeners();

    IGoalService getStepGoal();
}
