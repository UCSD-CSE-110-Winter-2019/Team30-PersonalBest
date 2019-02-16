package team30.personalbest;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.util.Consumer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import team30.personalbest.fitness.GoogleFitAdapter;
import team30.personalbest.fitness.OnGoogleFitReadyListener;
import team30.personalbest.fitness.service.ActiveFitnessService;
import team30.personalbest.fitness.service.FitnessService;
import team30.personalbest.goal.CustomGoalAchiever;
import team30.personalbest.goal.GoalAchiever;
import team30.personalbest.goal.GoalListener;
import team30.personalbest.goal.StepGoal;

public class MainActivity extends AppCompatActivity implements OnGoogleFitReadyListener, GoalListener
{
    private GoogleFitAdapter googleFitAdapter;
    private FitnessService fitnessService;
    private ActiveFitnessService activeFitnessService;

    private GoalAchiever goalAchiever;

    //private Button updateDailyStep_btn;
    //private TextView dailyStep_textView;

    private Button startButton;
    private Button endButton;
    private Button setNewGoalButton;
    private Button launchWeeklySnapshot;
    private TextView stepsGoalText;
    private TextView totalRunStepsText;
    private TextView currStepsText;
    private TextView timeElapsedText;
    private TextView mphText;
    private TextView heightText;
    private double timeElapsed;
    private double mph;
    private int totalRunSteps;
    private float height;
    private String goal_Text = "";
    private String height_Text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.googleFitAdapter = new GoogleFitAdapter(this);

        this.fitnessService = new FitnessService(this.googleFitAdapter);
        this.activeFitnessService = new ActiveFitnessService(this.googleFitAdapter);

        this.goalAchiever = new CustomGoalAchiever();
        this.goalAchiever.addGoalListener(this);
        //Update step goal to match current user goal...
        //this.goalAchiever.setStepGoal(new CustomStepGoal());

        //this.updateDailyStep_btn = (Button) findViewById(R.id.getDailyStepCount);
        //this.dailyStep_textView = (TextView) findViewById(R.id.StepCountTemp);

        this.googleFitAdapter.addOnReadyListener(this);
        this.googleFitAdapter.onActivityCreate(this, savedInstanceState);

        /** GUI STUFF */

        // Prompt height on initial launch of app
        if(height == 0) { // TODO: Needs to be changed when we get height
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Enter your height in meters");

            // Set up the input
            final EditText input = new EditText(MainActivity.this);

            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    height_Text = input.getText().toString();
                    heightText.setText("Your Height in Meters: " + height_Text);
                    height = Float.parseFloat(height_Text);
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

        // Switch to weekly snapshot
        launchWeeklySnapshot = findViewById(R.id.button_weekly_stats);
        launchWeeklySnapshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchActivity();
            }
        });

        // Run statistic textview
        totalRunStepsText = findViewById(R.id.total_steps);
        stepsGoalText = findViewById(R.id.steps_goal);
        currStepsText = findViewById(R.id.curr_steps);
        timeElapsedText = findViewById(R.id.time_elapsed);
        mphText = findViewById(R.id.mph);
        heightText = findViewById(R.id.heightText);

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        this.googleFitAdapter.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onGoogleFitReady(final GoogleFitAdapter googleFitAdapter)
    {
        /*
        this.updateDailyStep_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.fitnessService.getFitnessSnapshot()
                        .onResult(new Consumer<IFitnessSnapshot>() {
                            @Override
                            public void accept(IFitnessSnapshot iFitnessSnapshot) {
                                String totalSteps = "" + iFitnessSnapshot.getTotalSteps();
                                dailyStep_textView.setText(totalSteps);
                            }
                        });
            }
        });
        */
    }

    @Override
    public void onGoalAchievement(StepGoal goal)
    {
        //Achieved Goal!
        Toast.makeText(this, "Achievement get!", Toast.LENGTH_SHORT).show();
    }

    private void launchActivity()
    {
        Intent intent = new Intent(this, GraphActivity.class);
        startActivity(intent);
    }
}
