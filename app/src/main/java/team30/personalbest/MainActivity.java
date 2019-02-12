package team30.personalbest;

import android.support.v4.util.Consumer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Date;

import team30.personalbest.fitness.FitnessService;
import team30.personalbest.fitness.FitnessSnapshot;
import team30.personalbest.goal.CustomGoalAchiever;
import team30.personalbest.goal.CustomStepGoal;
import team30.personalbest.goal.GoalAchiever;
import team30.personalbest.goal.GoalListener;
import team30.personalbest.goal.StepGoal;
import team30.personalbest.fitness.GoogleFitnessService;
import team30.personalbest.walk.StepListener;
import team30.personalbest.walk.WalkSteps;
import team30.personalbest.walk.intentional.IntentionalWalkSteps;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TODO: This should REALLY not live here, but somewhere safe.
        final FitnessService fitnessService = new GoogleFitnessService(this);
        final WalkSteps walkSteps = new IntentionalWalkSteps(fitnessService);

        final StepGoal stepGoal = new CustomStepGoal(fitnessService);
        final GoalAchiever goalAchiever = new CustomGoalAchiever(stepGoal);

        //Initialize goal achiever (this should be called every startup and when step goal changes)
        goalAchiever.startAchievingGoal();

        //TODO: EVERYTHING BELOW HERE is trash and is just for an example.
        walkSteps.addStepListener(new StepListener() {
            @Override
            public void onStepUpdate(WalkSteps handler, FitnessSnapshot snapshot) {
                //Do something step-related....
            }
        });

        goalAchiever.addGoalListener(new GoalListener() {
            @Override
            public void onGoalAchievement(StepGoal goal) {
                //Do something goal-related...
            }
        });

        fitnessService.getFitnessSnapshot(0).onResult(new Consumer<FitnessSnapshot>() {
            @Override
            public void accept(FitnessSnapshot fitnessSnapshot) {
                //Do something with the most recent fitness snapshot data...
            }
        });

        fitnessService.getProgressSteps().onResult(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                //Do something with the progress data...
            }
        });
    }
}
