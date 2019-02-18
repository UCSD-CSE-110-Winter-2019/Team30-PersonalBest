package team30.personalbest.fitness.snapshot;

public interface IRecordingFitnessSnapshot extends IFitnessSnapshot
{
    IRecordingFitnessSnapshot addOnRecordingSnapshotUpdateListener(OnRecordingSnapshotUpdateListener listener);
}
