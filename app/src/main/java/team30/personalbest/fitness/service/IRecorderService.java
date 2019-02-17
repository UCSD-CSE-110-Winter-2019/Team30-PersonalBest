package team30.personalbest.fitness.service;

import android.app.Activity;
import android.os.Bundle;

import team30.personalbest.fitness.Callback;
import team30.personalbest.fitness.snapshot.IActiveFitnessSnapshot;
import team30.personalbest.fitness.snapshot.IFitnessSnapshot;

public interface IRecorderService
{
    IRecorderService addOnRecorderServiceReady(OnRecorderServiceReadyListener listener);
    void removeOnRecorderServiceReady(OnRecorderServiceReadyListener listener);

    /**You must call these to enable the fitness service*/
    void onActivityCreate(Activity activity, Bundle savedInstanceState);

    IActiveFitnessSnapshot startRecording();
    Callback<IFitnessSnapshot> stopRecording();
    boolean isRecording();

    IActiveFitnessSnapshot getRecordingSnapshot();
    Callback<Iterable<IFitnessSnapshot>> getRecordingSnapshots(long startTime, long stopTime);

    boolean isServiceReady();
}
