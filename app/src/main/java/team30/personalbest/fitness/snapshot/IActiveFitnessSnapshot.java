package team30.personalbest.fitness.snapshot;

public interface IActiveFitnessSnapshot extends IFitnessSnapshot
{
    IActiveFitnessSnapshot addOnActiveSnapshotUpdate(OnActiveSnapshotUpdateListener listener);
}
