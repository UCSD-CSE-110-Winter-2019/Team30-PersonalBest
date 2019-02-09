package team30.personalbest.walk;

import android.location.Location;

import java.util.Date;

import team30.personalbest.fitness.FitnessService;
import team30.personalbest.fitness.FitnessSnapshot;

public interface WalkSteps
{
    FitnessSnapshot startRecording(Location location, Date date);
    FitnessSnapshot stopRecording(Location location, Date date);

    //This is not async because we should always have some value to display
    FitnessSnapshot getActiveStats();
    FitnessService getFitnessService();

    WalkSteps addStepListener(StepListener listener);
    void removeStepListener(StepListener listener);
    void clearStepListeners();
    Iterable<StepListener> getStepListeners();
}
