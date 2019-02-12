package team30.personalbest.fitness;

public interface FitnessService
{
    Callback<FitnessSnapshot> getFitnessSnapshot(long time);
    Callback<Integer> getProgressSteps();
}
