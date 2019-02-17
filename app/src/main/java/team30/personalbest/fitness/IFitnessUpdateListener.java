package team30.personalbest.fitness;

import team30.personalbest.fitness.snapshot.IFitnessSnapshot;

public interface IFitnessUpdateListener
{
    void onStepUpdate(IFitnessSnapshot snapshot);
}
