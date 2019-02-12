package team30.personalbest.walk.intentional;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

import team30.personalbest.fitness.FitnessService;
import team30.personalbest.walk.WalkSteps;
import team30.personalbest.walk.StepListener;

public class IntentionalWalkSteps implements WalkSteps
{
    private final List<StepListener> listeners = new ArrayList<>();
    private final FitnessService fitnessService;

    public IntentionalWalkSteps(FitnessService fitnessService)
    {
        this.fitnessService = fitnessService;
    }

    //TODO: Call this from somewhere, starting from startRecording()
    protected void onIntentionalStepUpdate(IntentionalWalkStats currentWalkStats)
    {
        //TODO: Do anything else you want to update here on ACTIVE step

        //Call all listeners
        for(StepListener listener : this.listeners)
        {
            listener.onStepUpdate(this, currentWalkStats);
        }
    }

    @Override
    public IntentionalWalkStats startRecording(Location location, long time)
    {
        //TODO: Return a new IntentionalWalkStats object for others to use and save it
        //IntentionalWalkStats should be updated in real-time
        return null;
    }

    @Override
    public IntentionalWalkStats stopRecording(Location location, long time)
    {
        //TODO: Return the just finished IntentionalWalkStats
        return null;
    }

    @Override
    public IntentionalWalkStats getActiveStats()
    {
        //TODO: Return null if not yet started.
        return null;
    }

    @Override
    public FitnessService getFitnessService()
    {
        return this.fitnessService;
    }

    @Override
    public WalkSteps addStepListener(StepListener listener)
    {
        this.listeners.add(listener);
        return this;
    }

    @Override
    public void removeStepListener(StepListener listener)
    {
        this.listeners.remove(listener);
    }

    @Override
    public void clearStepListeners()
    {
        this.listeners.clear();
    }

    @Override
    public Iterable<StepListener> getStepListeners()
    {
        return this.listeners;
    }
}
