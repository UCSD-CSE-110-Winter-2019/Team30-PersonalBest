package team30.personalbest.framework.snapshot;

public interface IRecordingFitnessSnapshot extends IFitnessSnapshot {
    IRecordingFitnessSnapshot addOnRecordingSnapshotUpdateListener(OnRecordingSnapshotUpdateListener listener);
}
