package team30.personalbest.goal;

import android.os.AsyncTask;
import android.support.v4.util.Consumer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import team30.personalbest.fitness.service.IFitnessService;
import team30.personalbest.fitness.snapshot.IFitnessSnapshot;

public class CustomGoalAchiever implements GoalAchiever {
    private final List<GoalListener> listeners = new ArrayList<>();
    private StepGoal goal;
    private boolean running = false;
    private IFitnessService fitnessService;
    private GoalChecker goalChecker;

    public CustomGoalAchiever(IFitnessService fs) {
        this.fitnessService = fs;
    }

    public boolean isRunning() {
        return this.running;
    }

    @Override
    public CustomGoalAchiever setStepGoal(StepGoal goal) {
        if (this.running) throw new IllegalStateException("Cannot change step goal while running");
        this.goal = goal;
        return this;
    }

    @Override
    public void startAchievingGoal() {
        if (this.running) throw new IllegalStateException("Already started.");
        if (this.goal == null) throw new IllegalStateException("Missing step goal");

        this.running = true;

        if (this.goalChecker != null && !this.goalChecker.isCancelled()) {
            goalChecker.cancel(true);
        }

        this.goalChecker = new GoalChecker(this, this.fitnessService, this.goal);
        this.goalChecker.execute();
    }

    @Override
    public void stopAchievingGoal() {
        if (!this.running) throw new IllegalStateException("Not yet started.");

        this.running = false;
        if (this.goalChecker != null && !this.goalChecker.isCancelled()) {
            this.goalChecker.cancel(true);
        }

    }

    @Override
    public void doAchieveGoal() {
        for (GoalListener listener : this.listeners) {
            listener.onGoalAchievement(this.goal);
        }
    }

    public void doAchieveSubGoal() {
        for(GoalListener listener : this.listeners) {
            listener.onSubGoalAchievement(this.goal);
        }
    }

    @Override
    public CustomGoalAchiever addGoalListener(GoalListener listener) {
        this.listeners.add(listener);
        return this;
    }

    @Override
    public void removeGoalListener(GoalListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void clearGoalListeners() {
        this.listeners.clear();
    }

    @Override
    public Iterable<GoalListener> getGoalListeners() {
        return this.listeners;
    }

    @Override
    public StepGoal getStepGoal() {
        return this.goal;
    }

    public boolean hasSignificantlyImproved() {
        if (this.goalChecker == null) return false;
        return this.goalChecker.hasImproved();
    }

    private static class GoalChecker extends AsyncTask<Void, Void, Void> {
        public static final String TAG = "GoalChecker";

        private final GoalAchiever achiever;
        private final IFitnessService fitnessService;
        private final StepGoal stepGoal;

        private boolean subGoalAchieved = false;
        private boolean hasImproved = false;
        private int previousSteps = 0;
        private int goalSteps;
        private int currentSteps;
        private int timeInterval = 4000; // Check every 4 seconds

        public GoalChecker(GoalAchiever goalAchiever, IFitnessService fitnessService, StepGoal stepGoal) {
            this.achiever = goalAchiever;
            this.fitnessService = fitnessService;
            this.stepGoal = stepGoal;
        }

        @Override
        protected void onPreExecute() {

            this.previousSteps = 0;
            // Get steps from previous day
            Calendar endTime = Calendar.getInstance();
            Calendar startTime = Calendar.getInstance();

            startTime.set(Calendar.HOUR_OF_DAY, 0);
            startTime.set(Calendar.MINUTE, 0);
            startTime.set(Calendar.SECOND, 1);
            startTime.add(Calendar.DAY_OF_MONTH, -1);

            endTime.set(Calendar.HOUR_OF_DAY, 23);
            endTime.set(Calendar.MINUTE, 59);
            endTime.set(Calendar.SECOND, 59);
            endTime.add(Calendar.DAY_OF_MONTH, -1);

            this.fitnessService.getFitnessSnapshots(startTime.getTimeInMillis(), endTime.getTimeInMillis())
                    .onResult(new Consumer<Iterable<IFitnessSnapshot>>() {
                        @Override
                        public void accept(Iterable<IFitnessSnapshot> iFitnessSnapshots) {
                            for (IFitnessSnapshot snapshot : iFitnessSnapshots) {
                                GoalChecker.this.previousSteps += snapshot.getTotalSteps();
                            }
                        }
                    });

            this.fitnessService.getFitnessSnapshot().onResult(new Consumer<IFitnessSnapshot>() {
                @Override
                public void accept(IFitnessSnapshot fitnessSnapshot) {
                    GoalChecker.this.currentSteps = fitnessSnapshot.getTotalSteps();
                }
            });
        }

        @Override
        protected void onPostExecute(Void v) {
            if (this.currentSteps < this.goalSteps) {
                this.achiever.doAchieveGoal();
            }
        }

        @Override
        protected void onProgressUpdate(Void... v) {
            /* Maybe update steps real-time? */
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                while (this.currentSteps < this.goalSteps) {
                    if (!this.hasImproved && this.currentSteps > this.previousSteps) {
                        this.hasImproved = true;
                    }

                    if(!this.subGoalAchieved && this.currentSteps > (this.goalSteps / 7)) {
                        this.achiever.doAchieveSubGoal();
                        this.subGoalAchieved = true;
                    }

                    Thread.sleep(this.timeInterval);

                    this.fitnessService.getFitnessSnapshot().onResult(new Consumer<IFitnessSnapshot>() {
                        @Override
                        public void accept(IFitnessSnapshot fitnessSnapshot) {
                            GoalChecker.this.currentSteps = fitnessSnapshot.getTotalSteps();
                        }
                    });

                    this.stepGoal.getGoalValue().onResult(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer integer) {
                            GoalChecker.this.goalSteps = integer;
                        }
                    });

                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to update goal checker.", e);
            }

            return null;
        }

        public boolean hasImproved()
        {
            return this.hasImproved;
        }
    }
}
