package team30.personalbest.fitness;

import team30.personalbest.fitness.snapshot.IFitnessSnapshot;

public interface OnFitnessUpdateListener
{
    void onFitnessUpdate(IFitnessSnapshot fitnessSnapshot);
}
