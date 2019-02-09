package team30.personalbest.walk;

import team30.personalbest.fitness.FitnessSnapshot;

public interface StepListener
{
    void onStepUpdate(WalkSteps handler, FitnessSnapshot snapshot);
}
