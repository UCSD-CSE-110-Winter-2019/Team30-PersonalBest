package team30.personalbest.framework.snapshot;

public class GoalSnapshot implements IGoalSnapshot {
    private long goalTime;
    private int goalValue;

    @Override
    public int getGoalValue() {
        return this.goalValue;
    }

    public GoalSnapshot setGoalValue(int steps) {
        this.goalValue = steps;
        return this;
    }

    @Override
    public long getGoalTime() {
        return this.goalTime;
    }

    public GoalSnapshot setGoalTime(long millis) {
        this.goalTime = millis;
        return this;
    }
}
