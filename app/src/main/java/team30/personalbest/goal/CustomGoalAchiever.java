package team30.personalbest.goal;

import android.os.AsyncTask;
import android.support.v4.util.Consumer;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import team30.personalbest.fitness.service.FitnessService;
import team30.personalbest.fitness.snapshot.IFitnessSnapshot;

public class CustomGoalAchiever implements GoalAchiever
{
    private final List<GoalListener> listeners = new ArrayList<>();
    private StepGoal goal;
    private boolean running = false;
    private FitnessService fitnessService;
    private GoalChecker goalChecker;
    private boolean hasImporoved = false;
    private boolean achievedSubGoal = false;
    private int previousSteps = 0;


    public CustomGoalAchiever() {}

    public CustomGoalAchiever(StepGoal goal, FitnessService fs )
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
        if( goalChecker != null && !goalChecker.isCancelled() ) {
            goalChecker.cancel(true);
        }

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

        return hasImporoved;
    }

    public void doAchieveSubGoal() {
        for(GoalListener listener : this.listeners)
        {
            listener.onSubGoalAchievement(this.goal);
        }

        achievedSubGoal = true;
    }


    private class GoalChecker extends AsyncTask<Void, Void, Void> {

        private int steps;
        private int timeInterval = 4000; // Check every 4 seconds

        @Override
        protected Void doInBackground( Void... voids) {

            try{

                while( steps < goal.getGoalValue()  ){

                    if( !achievedSubGoal && steps > (goal.getGoalValue() / 7 ) ) {
                        doAchieveSubGoal();
                    }
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

            previousSteps = 0;
            // Get steps from previous day
            Calendar endTime = Calendar.getInstance();
            Calendar startTime = Calendar.getInstance();

            startTime.set(Calendar.HOUR_OF_DAY, 0);
            startTime.set(Calendar.MINUTE, 0 );
            startTime.set(Calendar.SECOND, 1);
            startTime.add( Calendar.DAY_OF_MONTH, -1 );


            endTime.set(Calendar.HOUR_OF_DAY, 23);
            endTime.set(Calendar.MINUTE,59 );
            endTime.set(Calendar.SECOND, 59);
            endTime.add( Calendar.DAY_OF_MONTH, -1 );



            fitnessService.getFitnessSnapshots( startTime.getTimeInMillis(), endTime.getTimeInMillis() )
                    .onResult(new Consumer<Iterable<IFitnessSnapshot>>() {
                        @Override
                        public void accept(Iterable<IFitnessSnapshot> snapshots) {

                            for( IFitnessSnapshot snapshot : snapshots ) {
                                previousSteps += snapshot.getTotalSteps();
                            }
                        }
                    });

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
