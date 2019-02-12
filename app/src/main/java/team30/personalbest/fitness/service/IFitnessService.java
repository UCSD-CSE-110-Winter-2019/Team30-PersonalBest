package team30.personalbest.fitness.service;

import team30.personalbest.fitness.Callback;
import team30.personalbest.fitness.snapshot.IFitnessSnapshot;

public interface IFitnessService
{
    Callback<Iterable<IFitnessSnapshot>> getFitnessSnapshots(long startTime, long stopTime);
    Callback<IFitnessSnapshot> getFitnessSnapshot();
}
