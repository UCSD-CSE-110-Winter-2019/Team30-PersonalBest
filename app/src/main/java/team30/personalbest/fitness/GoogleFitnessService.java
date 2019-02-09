package team30.personalbest.fitness;

import java.util.Date;

import team30.personalbest.fitness.Callback;
import team30.personalbest.fitness.FitnessService;
import team30.personalbest.fitness.FitnessSnapshot;
import team30.personalbest.walk.WalkStats;

public class GoogleFitnessService implements FitnessService
{
    @Override
    public Callback<FitnessSnapshot> getFitnessSnapshot(Date date)
    {
        //Prepare the async callback...
        final Callback<FitnessSnapshot> callback = new Callback<>();

        //TODO: Get all data for the whole day of date (ignore time)
        //This can be called at any time.
        //Construct this.
        WalkStats result = null;

        //Make this call when your async call finishes.
        callback.resolve(result);

        //Return the async callback
        return callback;
    }

    @Override
    public Callback<Integer> getProgressSteps()
    {
        //Prepare the async callback...
        final Callback<Integer> callback = new Callback<>();

        //TODO: Get steps since last goal met.
        //We can either store the time at which a goal was met
        //Or store the total steps before a goal was met
        //Or something to calculate how much progress was made since last goal.

        //Compute this.
        int result = 0;

        //Make this call when your async call finishes.
        callback.resolve(result);

        //Return the async callback
        return callback;
    }
}
