package team30.personalbest.goal;

import team30.personalbest.fitness.Callback;

public interface StepGoal
{
    Callback<Iterable<Integer>> getGoalValues(long startTime, long stopTime);
    Callback<Integer> getGoalValue();
}
