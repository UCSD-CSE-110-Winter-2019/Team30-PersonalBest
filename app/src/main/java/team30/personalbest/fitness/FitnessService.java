package team30.personalbest.fitness;

import java.util.Date;

public interface FitnessService
{
    Callback<FitnessSnapshot> getFitnessSnapshot(Date date);
    Callback<Integer> getProgressSteps();
}
