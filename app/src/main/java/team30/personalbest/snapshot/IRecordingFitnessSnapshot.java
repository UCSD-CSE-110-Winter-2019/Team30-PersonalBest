package team30.personalbest.snapshot;

public interface IRecordingFitnessSnapshot extends IFitnessSnapshot
{
	IRecordingFitnessSnapshot addOnRecordingSnapshotUpdateListener(OnRecordingSnapshotUpdateListener listener);
}
