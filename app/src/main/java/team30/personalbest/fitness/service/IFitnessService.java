package team30.personalbest.fitness.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import team30.personalbest.fitness.Callback;
import team30.personalbest.fitness.snapshot.IFitnessSnapshot;

public interface IFitnessService
{
    IFitnessService addOnFitnessServiceReady(OnFitnessServiceReadyListener listener);

    void onActivityCreate(Activity activity, Bundle savedInstanceState);
    void onActivityResult(Activity activity, int requestCode, int resultCode, @Nullable Intent data);

    void startRecording();
    Callback<IFitnessSnapshot> stopRecording();
    boolean isRecording();

    Callback<Iterable<IFitnessSnapshot>> getFitnessSnapshots(long startTime, long stopTime);
    Callback<IFitnessSnapshot> getFitnessSnapshot();
    long getCurrentTime();
    boolean isServiceReady();
}
