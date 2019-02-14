package team30.personalbest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import team30.personalbest.fitness.GoogleFitAdapter;

public class MainActivity extends AppCompatActivity {
    private GoogleFitAdapter googleFitAdapter;

    Button startButton;
    Button endButton;
    TextView stepsGoalText;
    TextView totalRunStepsText;
    TextView currStepsText;
    TextView timeElapsedText;
    TextView mphText;
    double timeElapsed;
    double mph;
    int totalRunSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.googleFitAdapter = new GoogleFitAdapter();

        // Run statistic textview
        totalRunStepsText = findViewById(R.id.total_steps);
        stepsGoalText = findViewById(R.id.steps_goal);
        currStepsText = findViewById(R.id.curr_steps);
        timeElapsedText = findViewById(R.id.time_elapsed);
        mphText = findViewById(R.id.mph);

        // Will only show during run
        currStepsText.setVisibility(View.INVISIBLE);
        timeElapsedText.setVisibility(View.INVISIBLE);
        mphText.setVisibility(View.INVISIBLE);

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
                //.startRecording(null, null);

               // Make sure statistics are shown
               currStepsText.setVisibility(View.VISIBLE);
               timeElapsedText.setVisibility(View.VISIBLE);
               mphText.setVisibility(View.VISIBLE);
           }
        });


        endButton.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
               // Show start button again
               startButton.setVisibility(View.VISIBLE);
               endButton.setVisibility(View.GONE);

                // Get current walk step number
                //walkSteps.stopRecording(null, null);

                // Display stats after run

           /*    totalRunSteps = walkSteps.getActiveStats().getTotalSteps(StepType.INTENTIONAL);
               totalRunStepsText.setText("Total Steps Taken: "+totalRunSteps);

               distanceRun = walkSteps.getActiveStats().getDistanceTravelled();
               distanceRunText.setText("Total Distance Travelled: "+distanceRun);

               velocityRun = walkSteps.getActiveStats().getMilesPerHour();
               velocityRunText.setText("Average Velocity: "+velocityRun);
               */
               // Show statistics
        //        totalRunStepsText.setVisibility(View.VISIBLE);
        //        distanceRunText.setVisibility(View.VISIBLE);
        //        velocityRunText.setVisibility(View.VISIBLE);
            }
        });

        this.googleFitAdapter.onActivityCreate(this, savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        this.googleFitAdapter.onActivityResult(this, requestCode, resultCode, data);
    }
}
