package team30.personalbest;

import android.support.v4.util.Consumer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Date;

import team30.personalbest.fitness.FitnessService;
import team30.personalbest.fitness.FitnessSnapshot;
import team30.personalbest.fitness.StepType;
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
    Button startButton;
    Button endButton;
    TextView totalRunStepsText;
    TextView distanceRunText;
    TextView velocityRunText;
    double distanceRun;
    double velocityRun;
    int totalRunSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: This should REALLY not live here, but somewhere safe.
        final FitnessService fitnessService = new GoogleFitnessService();
        final WalkSteps walkSteps = new IntentionalWalkSteps(fitnessService);
        final StepGoal stepGoal = new CustomStepGoal(fitnessService);
        final GoalAchiever goalAchiever = new CustomGoalAchiever(stepGoal);

       // Run statistic textview
       totalRunStepsText = findViewById(R.id.total_run_steps);
       distanceRunText = findViewById(R.id.distance_run);
       velocityRunText = findViewById(R.id.velocity_run);

       // Will only show after end run
       totalRunStepsText.setVisibility(View.INVISIBLE);
       distanceRunText.setVisibility(View.INVISIBLE);
       velocityRunText.setVisibility(View.INVISIBLE);

       // Start Walk/Run buttons
       startButton = findViewById(R.id.button_start);
       endButton = findViewById(R.id.button_end);

       // endButton invisible/gone in the beginning
       endButton.setVisibility(View.GONE);

       startButton.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
               // Set endButton visible
               startButton.setVisibility(View.GONE);
               endButton.setVisibility(View.VISIBLE);

               // Get current walk step number
               walkSteps.startRecording(null, null);

               // Make sure statistics is hidden
               totalRunStepsText.setVisibility(View.INVISIBLE);
               distanceRunText.setVisibility(View.INVISIBLE);
               velocityRunText.setVisibility(View.INVISIBLE);
           }
       });


       endButton.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
               // Show start button again
               startButton.setVisibility(View.VISIBLE);
               endButton.setVisibility(View.GONE);

               // Get current walk step number
               walkSteps.stopRecording(null, null);

               // Display stats after run

           /*    totalRunSteps = walkSteps.getActiveStats().getTotalSteps(StepType.INTENTIONAL);
               totalRunStepsText.setText("Total Steps Taken: "+totalRunSteps);

               distanceRun = walkSteps.getActiveStats().getDistanceTravelled();
               distanceRunText.setText("Total Distance Travelled: "+distanceRun);

               velocityRun = walkSteps.getActiveStats().getMilesPerHour();
               velocityRunText.setText("Average Velocity: "+velocityRun);
*/
               // Show statistics
               totalRunStepsText.setVisibility(View.VISIBLE);
               distanceRunText.setVisibility(View.VISIBLE);
               velocityRunText.setVisibility(View.VISIBLE);

           }
       });


        //Initialize goal achiever (this should be called every startup and when step goal changes)
     /*   goalAchiever.startAchievingGoal();

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

        fitnessService.getFitnessSnapshot(new Date()).onResult(new Consumer<FitnessSnapshot>() {
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
        }); */
    }
}
