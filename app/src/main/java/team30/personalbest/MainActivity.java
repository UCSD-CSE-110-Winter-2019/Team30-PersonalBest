package team30.personalbest;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.util.Consumer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Iterator;

import team30.personalbest.fitness.GoogleFitAdapter;
import team30.personalbest.fitness.OnGoogleFitReadyListener;
import team30.personalbest.fitness.service.ActiveFitnessService;
import team30.personalbest.fitness.service.FitnessService;
import team30.personalbest.fitness.service.HeightService;
import team30.personalbest.fitness.snapshot.IFitnessSnapshot;
import team30.personalbest.goal.CustomGoalAchiever;
import team30.personalbest.goal.GoalAchiever;
import team30.personalbest.goal.GoalListener;
import team30.personalbest.goal.StepGoal;

public class MainActivity extends AppCompatActivity implements OnGoogleFitReadyListener, GoalListener
{
    public static final String TAG = "MainActivity";

    private static final String TITLE_HEIGHT_PROMPT = "Enter your height in meters:";
    private static final String TITLE_STEP_GOAL_PROMPT = "Set your new step goal:";

    private GoogleFitAdapter googleFitAdapter;

    private HeightService heightService;
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
    private String goal_Text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.googleFitAdapter = new GoogleFitAdapter(this);

        this.heightService = new HeightService(this.googleFitAdapter);
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

        // Switch to weekly snapshot
        this.launchWeeklySnapshot = findViewById(R.id.button_weekly_stats);
        this.launchWeeklySnapshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.launchGraphActivity();
            }
        });

        // Run statistic textview
        this.totalRunStepsText = findViewById(R.id.total_steps);
        this.stepsGoalText = findViewById(R.id.steps_goal);
        this.currStepsText = findViewById(R.id.curr_steps);
        this.timeElapsedText = findViewById(R.id.time_elapsed);
        this.mphText = findViewById(R.id.mph);
        this.heightText = findViewById(R.id.heightText);

        // Will only show during run
        this.currStepsText.setVisibility(View.INVISIBLE);
        this.timeElapsedText.setVisibility(View.INVISIBLE);
        this.mphText.setVisibility(View.INVISIBLE);

        // Start Walk/Run buttons
        this.startButton = findViewById(R.id.button_start);
        this.endButton = findViewById(R.id.button_end);
        this.setNewGoalButton = findViewById(R.id.newStepGoalButton);

        // endButton invisible/gone in the beginning
        this.endButton.setVisibility(View.GONE);

        this.startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Set endButton visible
                MainActivity.this.startButton.setVisibility(View.GONE);
                MainActivity.this.endButton.setVisibility(View.VISIBLE);

                // Start recording current run
                MainActivity.this.activeFitnessService.startRecording();

                // Make sure statistics are shown
                MainActivity.this.currStepsText.setVisibility(View.VISIBLE);
                MainActivity.this.timeElapsedText.setVisibility(View.VISIBLE);
                MainActivity.this.mphText.setVisibility(View.VISIBLE);
            }
        });


        this.endButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Show start button again
                MainActivity.this.startButton.setVisibility(View.VISIBLE);
                MainActivity.this.endButton.setVisibility(View.GONE);

                // Stop recording current run
                MainActivity.this.activeFitnessService.stopRecording();

                // Display stats after run
                MainActivity.this.activeFitnessService.getFitnessSnapshot().onResult(
                        new Consumer<IFitnessSnapshot>() {
                            @Override
                            public void accept(IFitnessSnapshot iFitnessSnapshot) {
                                if (iFitnessSnapshot == null)
                                    throw new IllegalStateException("Unable to find recent valid active fitness snapshot");

                                final double mph = iFitnessSnapshot.getMilesPerHour();
                                final long duration = iFitnessSnapshot.getStopTime() - iFitnessSnapshot.getStartTime();
                                final int steps = iFitnessSnapshot.getTotalSteps();

                                MainActivity.this.totalRunStepsText.setText(steps + " steps");
                            }
                        }
                );

                // Display stats after run
                MainActivity.this.fitnessService.getFitnessSnapshot().onResult(
                        new Consumer<IFitnessSnapshot>() {
                            @Override
                            public void accept(IFitnessSnapshot iFitnessSnapshot) {
                                if (iFitnessSnapshot == null)
                                    throw new IllegalStateException("Unable to find recent valid active fitness snapshot");

                                final double mph = iFitnessSnapshot.getMilesPerHour();
                                final long duration = iFitnessSnapshot.getStopTime() - iFitnessSnapshot.getStartTime();
                                final int steps = iFitnessSnapshot.getTotalSteps();

                                MainActivity.this.currStepsText.setText(steps + " steps");
                                MainActivity.this.timeElapsedText.setText(duration + " ms");
                                MainActivity.this.mphText.setText(mph + " mph");
                            }
                        }
                );
            }
        });

        // Listener for New step steps_goal

        this.setNewGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.showGoalPrompt();
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
        // Prompt height on initial launch of app (after google fit is ready)
        this.heightService.getHeight().onResult(new Consumer<Float>() {
            @Override
            public void accept(Float aFloat) {
                if (aFloat == null)
                {
                    //Height was never set.
                    MainActivity.this.showHeightPrompt();
                }
                else
                {
                    //Height is already set.
                    //TODO: This is just to show that height was set. remove this later
                    heightText.setText("Your Height in Meters: " + aFloat.toString());
                }
            }
        });


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

    private void updateStepCount()
    {

    }

    private void showHeightPrompt()
    {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(TITLE_HEIGHT_PROMPT)
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try
                        {
                            final String heightString = input.getText().toString();
                            final float heightFloat = Float.parseFloat(heightString);

                            //TODO: This is just to show that height was set. remove this later
                            heightText.setText("Your Height in Meters: " + heightString);

                            heightService.setHeight(heightFloat).onResult(new Consumer<Float>() {
                                @Override
                                public void accept(Float aFloat) {
                                    if (aFloat == null)
                                        throw new IllegalArgumentException(
                                                "Unable to set height for google services"
                                        );

                                    Log.i(TAG, "Successfully processed height");
                                }
                            });
                        }
                        catch (Exception e)
                        {
                            Log.w(TAG, "Failed to process height", e);

                            showHeightPrompt();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        showHeightPrompt();
                    }
                });
        builder.show();
    }

    private void showGoalPrompt()
    {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(TITLE_STEP_GOAL_PROMPT)
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try
                        {
                            //TODO: set step goal here.
                            final String goalString = input.getText().toString();
                            final int goalInteger = Integer.parseInt(goalString);

                            //TODO: This is just to show that height was set. remove this later
                            stepsGoalText.setText("New Step Goal: "+goal_Text);

                            Log.i(TAG, "Successfully processed step goal");
                        }
                        catch (Exception e)
                        {
                            Log.w(TAG, "Failed to process step goal", e);

                            showGoalPrompt();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    private void launchGraphActivity()
    {
        final long currentTime = this.googleFitAdapter.getCurrentTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        final long sundayTime = calendar.getTimeInMillis();

        final long minTime = Math.min(sundayTime, currentTime);
        final long maxTime = Math.max(sundayTime, currentTime);
        this.fitnessService.getFitnessSnapshots(minTime, maxTime)
                .onResult(new Consumer<Iterable<IFitnessSnapshot>>() {
                    @Override
                    public void accept(final Iterable<IFitnessSnapshot> incidentalSnapshots) {
                        if (incidentalSnapshots == null)
                            throw new IllegalStateException("Cannot find valid incidental fitness snapshots");

                        MainActivity.this.activeFitnessService.getFitnessSnapshots(minTime, maxTime)
                                .onResult(new Consumer<Iterable<IFitnessSnapshot>>() {
                                    @Override
                                    public void accept(final Iterable<IFitnessSnapshot> intentionalSnapshots) {
                                        if (intentionalSnapshots == null)
                                            throw new IllegalStateException("Cannot find valid intentional fitness snapshots");

                                        final Intent intent = new Intent(MainActivity.this, GraphActivity.class);
                                        final Bundle weeklyBundle = MainActivity.this.buildWeeklyBundle(incidentalSnapshots, intentionalSnapshots);

                                        final Bundle bundle = new Bundle();
                                        bundle.putBundle(GraphActivity.BUNDLE_WEEKLY_STATS, weeklyBundle);
                                        intent.putExtras(bundle);

                                        MainActivity.this.startActivity(intent);
                                    }
                                });
                    }
                });
    }

    private Bundle buildWeeklyBundle(Iterable<IFitnessSnapshot> incidentalSnapshots, Iterable<IFitnessSnapshot> intentionalSnapshots)
    {
        final Iterator<IFitnessSnapshot> incidentalIterator = incidentalSnapshots.iterator();
        final Iterator<IFitnessSnapshot> intentionalIterator = intentionalSnapshots.iterator();

        final Bundle result = new Bundle();

        int dayCount = 0;
        while(dayCount < GraphActivity.BUNDLE_WEEK_LENGTH)
        {
            final Bundle dailyBundle = new Bundle();

            //Calculate incidental
            if (incidentalIterator.hasNext())
            {
                IFitnessSnapshot snapshot = incidentalIterator.next();
                dailyBundle.putInt(GraphActivity.BUNDLE_DAILY_STEPS,
                        snapshot.getTotalSteps());
                //dailyBundle.putInt(GraphActivity.BUNDLE_DAILY_GOALS, snapshot.getGoal());
            }

            //Calculate intentional
            if (intentionalIterator.hasNext())
            {
                IFitnessSnapshot snapshot = intentionalIterator.next();
                dailyBundle.putInt(GraphActivity.BUNDLE_DAILY_ACTIVE_STEPS,
                        snapshot.getTotalSteps());
                dailyBundle.putLong(GraphActivity.BUNDLE_DAILY_TIMES,
                        snapshot.getStopTime() - snapshot.getStartTime());
                dailyBundle.putDouble(GraphActivity.BUNDLE_DAILY_MPH,
                        snapshot.getMilesPerHour());
                dailyBundle.putDouble(GraphActivity.BUNDLE_DAILY_DISTANCE,
                        snapshot.getDistanceTravelled());
            }

            //Insert into result
            result.putBundle(GraphActivity.BUNDLE_WEEKLY_PREFIX + dayCount, dailyBundle);
            ++dayCount;
        }

        return result;
    }
}
