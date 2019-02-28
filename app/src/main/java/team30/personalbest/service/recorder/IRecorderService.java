package team30.personalbest.service.recorder;

import team30.personalbest.util.Callback;
import team30.personalbest.snapshot.IFitnessSnapshot;
import team30.personalbest.snapshot.IRecordingFitnessSnapshot;
import team30.personalbest.service.IService;

public interface IRecorderService extends IService
{
	IRecordingFitnessSnapshot startRecording();

	Callback<IFitnessSnapshot> stopRecording();

	boolean isRecording();

	IRecordingFitnessSnapshot getRecordingSnapshot();

	Callback<Iterable<IFitnessSnapshot>> getRecordingSnapshots(long startTime, long stopTime);
}
