package team30.personalbest.goal;

import java.util.ArrayList;
import java.util.List;

public class CustomGoalAchiever implements GoalAchiever
{
    private final List<GoalListener> listeners = new ArrayList<>();
    private StepGoal goal;
    private boolean running = false;

    public CustomGoalAchiever() {}

    public CustomGoalAchiever(StepGoal goal)
    {
        this.goal = goal;
    }

    public boolean isRunning()
    {
        return this.running;
    }

    @Override
    public CustomGoalAchiever setStepGoal(StepGoal goal)
    {
        if (this.running) throw new IllegalStateException("Cannot change step goal while running");
        this.goal = goal;
        return this;
    }

    @Override
    public void startAchievingGoal()
    {
        if (this.running) throw new IllegalStateException("Already started.");
        if (this.goal == null) throw new IllegalStateException("Missing step goal");

        this.running = true;

        //TODO: Start checking if the goal is achieved...
        //Start some AsyncTask that will intermittently check the current steps
        //Versus the step goal and call everyone.
        //Will need to call doAchieveGoal when achieved.

        //If achieved... call:
        this.doAchieveGoal();
    }

    @Override
    public void stopAchievingGoal()
    {
        if (!this.running) throw new IllegalStateException("Not yet started.");

        this.running = false;

        //TODO: Stop checking if the goal is achieved...
    }

    @Override
    public void doAchieveGoal()
    {
        for(GoalListener listener : this.listeners)
        {
            listener.onGoalAchievement(this.goal);
        }
    }

    @Override
    public CustomGoalAchiever addGoalListener(GoalListener listener)
    {
        this.listeners.add(listener);
        return this;
    }

    @Override
    public void removeGoalListener(GoalListener listener)
    {
        this.listeners.remove(listener);
    }

    @Override
    public void clearGoalListeners()
    {
        this.listeners.clear();
    }

    @Override
    public Iterable<GoalListener> getGoalListeners()
    {
        return this.listeners;
    }

    @Override
    public StepGoal getStepGoal()
    {
        return this.goal;
    }
}
