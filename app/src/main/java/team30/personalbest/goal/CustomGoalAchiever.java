package team30.personalbest.goal;

import android.os.AsyncTask;
import android.support.v4.util.Consumer;

import java.util.ArrayList;
import java.util.List;

import team30.personalbest.fitness.service.IFitnessService;
import team30.personalbest.fitness.snapshot.IFitnessSnapshot;

public class CustomGoalAchiever implements GoalAchiever
{
    private final List<GoalListener> listeners = new ArrayList<>();
    private StepGoal goal;
    private boolean running = false;
    private IFitnessService fitnessService;
    private GoalChecker goalChecker;
    private boolean hasImporoved = false;

    public CustomGoalAchiever() {}

    public CustomGoalAchiever(StepGoal goal, IFitnessService fs )
    {
        this.fitnessService = fs;
        this.goal = goal;
    }

    public boolean isRunning()
    {
        return this.running;
    }

    @Override
    public CustomGoalAchiever setStepGoal(StepGoal goal)
    {
        if (this.running) throw new IllegalStateException("Cannot change step goal while running");
        this.goal = goal;
        return this;
    }

    @Override
    public void startAchievingGoal()
    {
        if (this.running) throw new IllegalStateException("Already started.");
        if (this.goal == null) throw new IllegalStateException("Missing step goal");

        this.running = true;

        //TODO: Start checking if the goal is achieved...
        //Start some AsyncTask that will intermittently check the current steps
        //Versus the step goal and call everyone.
        //Will need to call doAchieveGoal when achieved.

        //If achieved... call:

        if( goalChecker != null && !goalChecker.isCancelled() ){
            goalChecker.cancel(true);
        }

        goalChecker = new GoalChecker();
        goalChecker.execute();
    }

    @Override
    public void stopAchievingGoal()
    {
        if (!this.running) throw new IllegalStateException("Not yet started.");

        this.running = false;

        //TODO: Stop checking if the goal is achieved...
    }

    @Override
    public void doAchieveGoal()
    {
        for(GoalListener listener : this.listeners)
        {
            listener.onGoalAchievement(this.goal);
        }
    }

    @Override
    public CustomGoalAchiever addGoalListener(GoalListener listener)
    {
        this.listeners.add(listener);
        return this;
    }

    @Override
    public void removeGoalListener(GoalListener listener)
    {
        this.listeners.remove(listener);
    }

    @Override
    public void clearGoalListeners()
    {
        this.listeners.clear();
    }

    @Override
    public Iterable<GoalListener> getGoalListeners()
    {
        return this.listeners;
    }

    @Override
    public StepGoal getStepGoal()
    {
        return this.goal;
    }

    public boolean hasSignificantlyImproved() {

        /* TODO: Check for imporvements.
         * Perhaps do while updating steps on GoalChecker?
         */
        return hasImporoved;
    }

    private class GoalChecker extends AsyncTask<Void, Void, Void> {

        private int steps;
        private int timeInterval = 4000; // Check every 4 seconds

        @Override
        protected Void doInBackground( Void... voids) {

            try{

                while( steps < goal.getGoalValue()  ){
                    Thread.sleep(timeInterval);
                    updateSteps();
                }
            } catch( Exception e ) {

            }
            return voids[0];
        }


        @Override
        protected void onPostExecute( Void v) {
            if( steps < goal.getGoalValue() ) {
                doAchieveGoal();
            }
        }



        @Override
        protected void onPreExecute() {
            updateSteps();
        }

        @Override
        protected void onProgressUpdate( Void... v ) {

            /* Maybe update steps real-time?
             */
        }

        protected void updateSteps() {
            fitnessService.getFitnessSnapshot().onResult(new Consumer<IFitnessSnapshot>() {
                @Override
                public void accept(IFitnessSnapshot iFitnessSnapshot) {
                    steps = iFitnessSnapshot.getTotalSteps();
                }
            });

        }

    }
}
