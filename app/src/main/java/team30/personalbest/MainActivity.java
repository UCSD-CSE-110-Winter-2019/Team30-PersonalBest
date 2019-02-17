package team30.personalbest;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.util.Consumer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Iterator;

import team30.personalbest.fitness.FitnessChecker;
import team30.personalbest.fitness.IFitnessUpdateListener;
import team30.personalbest.fitness.service.GoogleFitAdapter;
import team30.personalbest.fitness.service.HeightService;
import team30.personalbest.fitness.service.IFitnessService;
import team30.personalbest.fitness.service.OnFitnessServiceReadyListener;
import team30.personalbest.fitness.snapshot.IFitnessSnapshot;
import team30.personalbest.goal.CustomGoalAchiever;
import team30.personalbest.goal.GoalAchiever;
import team30.personalbest.goal.GoalListener;
import team30.personalbest.goal.StepGoal;

public class MainActivity extends AppCompatActivity implements OnFitnessServiceReadyListener,
        GoalListener, IFitnessUpdateListener
{
    public static final String TAG = "MainActivity";

    private static final String TITLE_HEIGHT_PROMPT = "Enter your height in meters:";
    private static final String TITLE_STEP_GOAL_PROMPT = "Set your new step goal:";

    private GoogleFitAdapter fitnessService;
    private HeightService heightService;

    private FitnessChecker fitnessChecker;
    private GoalAchiever goalAchiever;

    private StepGoal stepGoal;
    private StepGoal subStepGoal;

    private Button startButton;
    private Button endButton;
    private Button newGoalButton;
    private Button weeklySnapshotButton;
    private TextView stepsGoalText;
    private TextView totalRunStepsText;
    private TextView currStepsText;
    private TextView timeElapsedText;
    private TextView mphText;
    private TextView heightText;

    // Mocking variables
    private Button submitTime;
    private EditText timeSubmitText;

    // Time variable
    private long currTime;
    private long lastCheckedTime;
    private long fromMidnight;
    private long oneHour = 3600000;
    private long MILLIS_PER_DAY = oneHour * 24;//TimeUnit.DAYS.toMillis(1);
    private long twentyOClock;
    private long eightOClock;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.fitnessService = new GoogleFitAdapter(this)
                .addOnFitnessServiceReady(this);
        this.heightService = new HeightService(this, this.fitnessService);

        this.fitnessChecker = new FitnessChecker(this.fitnessService);
        this.fitnessChecker.addListener(this);

        this.goalAchiever = new CustomGoalAchiever();
        this.goalAchiever.addGoalListener(this);

        //Update step goal to match current user goal...
        //this.goalAchiever.setStepGoal(new CustomStepGoal());

        /** Add time edittext etc for mocking purposes */
        this.submitTime = findViewById(R.id.subTime);

        // TODO: Moved enocuragement stuff into own method

        /** GUI STUFF */

        final MainActivity activity = this;

        // Switch to weekly snapshot
        this.weeklySnapshotButton = findViewById(R.id.button_weekly_stats);
        this.weeklySnapshotButton.setOnClickListener(new View.OnClickListener() {
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
        this.newGoalButton = findViewById(R.id.newStepGoalButton);

        this.startButton.setEnabled(false);
        this.endButton.setEnabled(false);
        this.newGoalButton.setEnabled(false);
        this.weeklySnapshotButton.setEnabled(false);

        // endButton invisible/gone in the beginning
        this.endButton.setVisibility(View.GONE);

        this.startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Set endButton visible
                activity.startButton.setVisibility(View.GONE);
                activity.endButton.setVisibility(View.VISIBLE);

                // Start recording current run
                activity.fitnessService.startRecording();

                // Make sure statistics are shown
                activity.currStepsText.setVisibility(View.VISIBLE);
                activity.timeElapsedText.setVisibility(View.VISIBLE);
                activity.mphText.setVisibility(View.VISIBLE);
            }
        });

        this.endButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Show start button again
                activity.startButton.setVisibility(View.VISIBLE);
                activity.endButton.setVisibility(View.GONE);

                // Stop & display recorded current run
                activity.fitnessService.stopRecording().onResult(new Consumer<IFitnessSnapshot>() {
                    @Override
                    public void accept(IFitnessSnapshot fitnessSnapshot) {
                        if (fitnessSnapshot == null)
                        {
                            throw new IllegalStateException("Unable to find recent valid active fitness snapshot");
                        }

                        int steps = fitnessSnapshot.getTotalSteps();
                        double duration = (fitnessSnapshot.getStopTime() - fitnessSnapshot.getStartTime()) / 1000.0;
                        double speed = fitnessSnapshot.getSpeed();

                        Log.d(TAG, steps + " > " + duration + "s > " + speed + "mph");

                        //activity.totalRunStepsText.setText(steps + " steps");
                        activity.currStepsText.setText(steps + " steps");
                        activity.timeElapsedText.setText(duration + " s");
                        activity.mphText.setText(speed + " mph");
                    }
                });
            }
        });

        // Listener for New step steps_goal
        this.newGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.showGoalPrompt();
            }
        });

        //Initialize fitness service...
        this.fitnessService.onActivityCreate(this, savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        //Initialize fitness service...
        this.fitnessService.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onFitnessServiceReady(IFitnessService fitnessService)
    {
        this.startButton.setEnabled(true);
        this.endButton.setEnabled(true);
        this.newGoalButton.setEnabled(true);
        this.weeklySnapshotButton.setEnabled(true);

        // Prompt height on initial launch of app (after google fit is ready)
        this.heightService.getHeight().onResult(new Consumer<Float>() {
            @Override
            public void accept(Float aFloat) {
                Log.d(TAG, "Retrieved height...");
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

                //MainActivity.this.goalAchiever.startAchievingGoal();
            }
        });

        //Start checking for step updates
        this.fitnessChecker.startChecking();

        Log.i(TAG, "Successfully prepared app services");
    }

    @Override
    public void onGoalAchievement(StepGoal goal)
    {
        //TODO: Save timestamp to Shared Prefs here.

        //Achieved Goal!
        Toast.makeText(this, "Achievement get!", Toast.LENGTH_SHORT).show();
        showGoalPrompt();
    }

    //@Override
    public void onSubGoalAchievement(StepGoal goal)
    {
        // Achieved sub goal!
        Toast.makeText(this, "Achieved sub goal!", Toast.LENGTH_SHORT).show();
    }

    /** Mocking purposes **/
    public void onSubmitTime(View view) {
        this.timeSubmitText = findViewById(R.id.timeText);
        String thisCurrTime = timeSubmitText.getText().toString();

        if(thisCurrTime == null || thisCurrTime == "") {
            Toast.makeText(this, "Please enter a valid time!", Toast.LENGTH_LONG).show();
        }

        else {
            long currentTime = Long.parseLong(thisCurrTime);

            this.encouragement(currentTime);
            this.fitnessService.setCurrentTime(currentTime);
        }
    }


    /** Encouragement **/

    public void encouragement(long thisTime) {
        /** Show encouragement code */

        // Share pref for encouragement
        this.sharedPreferences = getSharedPreferences("user_name", MODE_PRIVATE);
        this.editor = sharedPreferences.edit();

        this.twentyOClock = MILLIS_PER_DAY - (oneHour * 4);
        this.eightOClock = MILLIS_PER_DAY - (oneHour * 16);

        // Grab the current time when app is open
        this.currTime = thisTime; // TODO: googleFitAdapter.getCurrentTime(); Also remember to change to GooglefitTime
        this.fromMidnight = lastCheckedTime % MILLIS_PER_DAY;
        this.lastCheckedTime = eightOClock; // TODO: sharedPreferences.getLong("lastcheckedtime", 0);

        if (this.currTime >= this.twentyOClock)
        {
            //TODO: replace this with a method call to isSignificantlyImproved()
            boolean significantlyImproved = true; //(Math.random() > 0.5);
            if (significantlyImproved)
            {
                boolean isSameDay = this.currTime - this.lastCheckedTime < this.MILLIS_PER_DAY;

                if (!isSameDay || this.fromMidnight <= this.twentyOClock)
                {
                    // Update shared pref and show message
                    Toast.makeText(this, "Good job you significantly improved your steps from yesterday!", Toast.LENGTH_LONG).show();
                    this.editor.putLong("lastcheckedtime", this.currTime);
                    this.editor.commit();
                }
            }
        }

        else if (this.currTime >= this.eightOClock)
        {
            //TODO: replace this with a method call to isSignificantlyImproved()
            boolean significantlyImproved = true; //(Math.random() > 0.5);
            if (significantlyImproved)
            {
                boolean isSameDay = this.currTime - this.lastCheckedTime < this.MILLIS_PER_DAY;
                if (!isSameDay && (this.fromMidnight) <= this.twentyOClock)
                {
                    //Show message and show message
                    Toast.makeText(this, "Good job you significantly improved your steps from yesterday!", Toast.LENGTH_LONG).show();
                    this.editor.putLong("lastcheckedtime", this.currTime);
                    this.editor.commit();
                }
            }
        }
    }

    @Override
    public void onStepUpdate(IFitnessSnapshot snapshot)
    {
        Log.i(TAG, "Updating step...");

        if (snapshot != null)
        {
            TextView currSteps = findViewById(R.id.curr_steps);
            currSteps.setText(snapshot.getTotalSteps() + " steps");
        }
    }

    private void showHeightPrompt()
    {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
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
                            stepsGoalText.setText("New Step Goal: " + goalString);

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
        final MainActivity activity = this;

        final long currentTime = this.fitnessService.getCurrentTime();
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

                        activity.fitnessService.getFitnessSnapshots(minTime, maxTime)
                                .onResult(new Consumer<Iterable<IFitnessSnapshot>>() {
                                    @Override
                                    public void accept(final Iterable<IFitnessSnapshot> intentionalSnapshots) {
                                        if (intentionalSnapshots == null)
                                            throw new IllegalStateException("Cannot find valid intentional fitness snapshots");

                                        final Intent intent = new Intent(activity, GraphActivity.class);
                                        final Bundle weeklyBundle = activity.buildWeeklyBundle(incidentalSnapshots, intentionalSnapshots);

                                        final Bundle bundle = new Bundle();
                                        bundle.putBundle(GraphActivity.BUNDLE_WEEKLY_STATS, weeklyBundle);
                                        intent.putExtras(bundle);

                                        activity.startActivity(intent);
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
                        snapshot.getSpeed());
            }

            //Insert into result
            result.putBundle(GraphActivity.BUNDLE_WEEKLY_PREFIX + dayCount, dailyBundle);
            ++dayCount;
        }

        return result;
    }
}
