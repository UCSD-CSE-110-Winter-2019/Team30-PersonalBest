package team30.personalbest.goal;

import team30.personalbest.fitness.Callback;

public interface IGoalService
{
    Callback<Integer> setGoalValue(int value);
    Callback<Iterable<Integer>> getGoalValues(long startTime, long stopTime);
    Callback<Integer> getGoalValue();
}
