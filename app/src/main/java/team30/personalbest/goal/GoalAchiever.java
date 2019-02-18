package team30.personalbest.goal;

public interface GoalAchiever
{
    GoalAchiever setStepGoal(StepGoal goal);

    void startAchievingGoal();
    void stopAchievingGoal();

    void doAchieveGoal();
    void doAchieveSubGoal();

    GoalAchiever addGoalListener(GoalListener listener);
    void removeGoalListener(GoalListener listener);
    void clearGoalListeners();
    Iterable<GoalListener> getGoalListeners();

    StepGoal getStepGoal();
}
