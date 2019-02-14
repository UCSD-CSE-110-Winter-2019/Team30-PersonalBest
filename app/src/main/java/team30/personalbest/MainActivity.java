package team30.personalbest;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.text.InputType;

import team30.personalbest.fitness.GoogleFitAdapter;

public class MainActivity extends AppCompatActivity {
    private GoogleFitAdapter googleFitAdapter;

    Button startButton;
    Button endButton;
    Button setNewGoalButton;
    TextView stepsGoalText;
    TextView totalRunStepsText;
    TextView currStepsText;
    TextView timeElapsedText;
    TextView mphText;
    double timeElapsed;
    double mph;
    int totalRunSteps;
    private String goal_Text = "";

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
        setNewGoalButton = findViewById(R.id.newStepGoalButton);

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

        // Listener for New step steps_goal

        setNewGoalButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Set New Step Goal");

                // Set up the input
                final EditText input = new EditText(MainActivity.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goal_Text = input.getText().toString();
                        stepsGoalText.setText("New Step Goal: "+goal_Text);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();


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
