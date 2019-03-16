package team30.personalbest.framework.service;

import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.snapshot.IFitnessSnapshot;
import team30.personalbest.framework.snapshot.IRecordingFitnessSnapshot;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.util.Callback;

public interface IRecordingService {
    IRecordingFitnessSnapshot startRecording(IFitnessUser user, IFitnessClock clock);

    Callback<IFitnessSnapshot> stopRecording(IFitnessUser user, IFitnessClock clock);

    IRecordingFitnessSnapshot getRecordingSnapshot(IFitnessUser user);

    boolean isRecording();
}
