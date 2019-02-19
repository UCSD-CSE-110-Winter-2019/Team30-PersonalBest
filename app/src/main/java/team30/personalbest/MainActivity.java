package team30.personalbest;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import team30.personalbest.goal.FitnessGoalAchiever;
import team30.personalbest.goal.GoalListener;
import team30.personalbest.service.OnServicesReadyListener;
import team30.personalbest.service.ServiceInitializer;
import team30.personalbest.service.fitness.GoogleFitAdapter;
import team30.personalbest.service.goal.GoalService;
import team30.personalbest.service.goal.IGoalService;
import team30.personalbest.service.height.HeightService;
import team30.personalbest.service.watcher.FitnessWatcher;
import team30.personalbest.service.watcher.OnFitnessUpdateListener;
import team30.personalbest.snapshot.IFitnessSnapshot;
import team30.personalbest.snapshot.IGoalSnapshot;
import team30.personalbest.snapshot.IRecordingFitnessSnapshot;
import team30.personalbest.snapshot.OnRecordingSnapshotUpdateListener;
import team30.personalbest.util.Callback;

public class MainActivity extends AppCompatActivity implements OnServicesReadyListener,
		GoalListener, OnRecordingSnapshotUpdateListener, OnFitnessUpdateListener
{
	public static final String TAG = "MainActivity";

	private static final String TITLE_HEIGHT_PROMPT = "Enter your height in meters:";
	private static final String TITLE_STEP_GOAL_PROMPT = "Set your new step goal:";

	public static final int DEFAULT_GOAL_VALUE = 500;

	private ServiceInitializer serviceInitializer;
	private GoogleFitAdapter fitnessService;
	private HeightService heightService;
	private FitnessWatcher fitnessWatcher;

	private GoalService goalService;
	private FitnessGoalAchiever goalAchiever;

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
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.fitnessService = new GoogleFitAdapter();
		this.heightService = new HeightService(this.fitnessService);
		this.fitnessWatcher = new FitnessWatcher(this.fitnessService);
		this.fitnessWatcher.addFitnessListener(this);
		this.goalService = new GoalService(this.fitnessService);

		this.serviceInitializer = new ServiceInitializer.Builder()
				.addService(this.fitnessService)
				.addService(this.heightService)
				.addService(this.goalService)
				.addService(this.fitnessWatcher)
				.build();
		this.serviceInitializer.addOnServicesReadyListener(this);

		this.goalAchiever = new FitnessGoalAchiever(this.goalService);
		this.goalAchiever.addGoalListener(this);
		this.fitnessWatcher.addFitnessListener(this.goalAchiever);

		/** Add time edittext etc for mocking purposes */
		this.submitTime = findViewById(R.id.subTime);

		// TODO: Moved enocuragement stuff into own method

		/** GUI STUFF */

		final MainActivity activity = this;

		// Switch to weekly snapshot
		this.weeklySnapshotButton = findViewById(R.id.button_weekly_stats);
		this.weeklySnapshotButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				activity.launchGraphActivity();
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

		this.startButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				// Set endButton visible
				activity.startButton.setVisibility(View.GONE);
				activity.endButton.setVisibility(View.VISIBLE);

				// Start recording current run
				activity.fitnessService.startRecording().addOnRecordingSnapshotUpdateListener(activity);

				// Make sure statistics are shown
				activity.currStepsText.setVisibility(View.VISIBLE);
				activity.timeElapsedText.setVisibility(View.VISIBLE);
				activity.mphText.setVisibility(View.VISIBLE);
			}
		});

		this.endButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				// Show start button again
				activity.startButton.setVisibility(View.VISIBLE);
				activity.endButton.setVisibility(View.GONE);

				// Stop & display recorded current run
				activity.fitnessService.stopRecording().onResult(new Consumer<IFitnessSnapshot>()
				{
					@Override
					public void accept(IFitnessSnapshot fitnessSnapshot)
					{
						if (fitnessSnapshot == null)
						{
							throw new IllegalStateException("Unable to find recent valid active fitness snapshot");
						}

						activity.displayRecordingSnapshot(fitnessSnapshot);
					}
				});
			}
		});

		// Listener for New step steps_goal
		this.newGoalButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				activity.showGoalPrompt(false);
			}
		});

		//Initialize services...
		this.serviceInitializer.onActivityCreate(this, savedInstanceState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
	{
		//Initialize services...
		this.serviceInitializer.onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	public void onServicesReady(ServiceInitializer serviceInitializer)
	{
		final MainActivity activity = this;

		// Prompt height on initial launch of app (after google fit is ready)
		this.resolveHeight().onResult(new Consumer<Float>()
		{
			@Override
			public void accept(Float aFloat)
			{
				if (aFloat == null)
				{
					throw new IllegalStateException("Unable to resolve height");
				}

				activity.heightText.setText(
						activity.getString(R.string.display_height,
						                   aFloat));

				//Enable the app interface
				activity.startButton.setEnabled(true);
				activity.endButton.setEnabled(true);
				activity.newGoalButton.setEnabled(true);
				activity.weeklySnapshotButton.setEnabled(true);

				//Get the current step goal
				activity.goalService.getGoalSnapshot().onResult(new Consumer<IGoalSnapshot>() {
					@Override
					public void accept(IGoalSnapshot iGoalSnapshot)
					{
						if (iGoalSnapshot == null)
						{
							activity.stepsGoalText.setText(
									activity.getString(R.string.display_stepgoal, DEFAULT_GOAL_VALUE));
						}
						else
						{
							activity.stepsGoalText.setText(
									activity.getString(R.string.display_stepgoal, iGoalSnapshot.getGoalValue()));
						}
					}
				});

				Log.i(TAG, "Successfully initialized app services");
			}
		});

		Log.i(TAG, "Successfully prepared app services");
	}

	@Override
	public void onFitnessUpdate(IFitnessSnapshot fitnessSnapshot)
	{
		if (fitnessSnapshot != null)
		{
			int steps = fitnessSnapshot.getTotalSteps();
			this.totalRunStepsText.setText(steps + " steps");
		}
	}

	@Override
	public void onGoalAchievement(IGoalService goal)
	{
		//TODO: Save timestamp to Shared Prefs here.

		//Achieved Goal!
		Toast.makeText(this, "Achieved step goal! Good job!", Toast.LENGTH_SHORT).show();
		showGoalPrompt(true);
	}

	@Override
	public void onSubGoalAchievement(IGoalService goal)
	{
		// Achieved sub goal!
		Toast.makeText(this, "Achieved daily goal! Nice work!", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Mocking purposes
	 **/
	public void onSubmitTime(View view)
	{
		this.timeSubmitText = findViewById(R.id.timeText);
		String thisCurrTime = timeSubmitText.getText().toString();

		if (thisCurrTime == null || thisCurrTime == "")
		{
			Toast.makeText(this, "Please enter a valid time!", Toast.LENGTH_LONG).show();
		}

		else
		{
			long currentTime = Long.parseLong(thisCurrTime);

			this.encouragement(currentTime);
			this.fitnessService.setCurrentTime(currentTime);
		}
	}


	/**
	 * Encouragement
	 **/

	public void encouragement(long thisTime)
	{
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
	public void onRecordingSnapshotUpdate(IRecordingFitnessSnapshot activeSnapshot)
	{
		Log.i(TAG, "Updating active step...");

		if (activeSnapshot != null)
		{
			this.displayRecordingSnapshot(activeSnapshot);
		}
	}

	private Callback<Float> resolveHeight()
	{
		final Callback<Float> callback = new Callback<>();
		this.heightService.getHeight().onResult(new Consumer<Float>() {
			@Override
			public void accept(Float aFloat)
			{
				Log.d(TAG, "Resolving height...");
				if (aFloat == null)
				{
					MainActivity.this.showHeightPrompt().onResult(new Consumer<Float>() {
						@Override
						public void accept(Float aFloat)
						{
							callback.resolve(aFloat);
						}
					});
				}
				else
				{
					callback.resolve(aFloat);
				}
			}
		});
		return callback;
	}

	private Callback<Float> showHeightPrompt()
	{
		final Callback<Float> callback = new Callback<>();
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		final AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setTitle(TITLE_HEIGHT_PROMPT)
				.setView(input)
				.setPositiveButton("OK", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						try
						{
							final String heightString = input.getText().toString();
							final float heightFloat = Float.parseFloat(heightString);

							//TODO: This is just to show that height was set. remove this later
							heightText.setText("Your Height in Meters: " + heightString);

							heightService.setHeight(heightFloat).onResult(new Consumer<Float>()
							{
								@Override
								public void accept(Float aFloat)
								{
									if (aFloat == null)
										throw new IllegalArgumentException(
												"Unable to set height for google services"
										);

									Log.i(TAG, "Successfully processed height");
									callback.resolve(aFloat);
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
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.cancel();
						showHeightPrompt();
					}
				});
		builder.show();
		return callback;
	}

	private void showGoalPrompt(final boolean forceMax)
	{
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		final AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setTitle(TITLE_STEP_GOAL_PROMPT)
				.setView(input)
				.setPositiveButton("OK", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						try
						{
							final String goalString = input.getText().toString();
							final int goalInteger = Integer.parseInt(goalString);
							MainActivity.this.goalService.setCurrentGoal(goalInteger);

							stepsGoalText.setText("Your Step Goal: " + goalString);

							Log.i(TAG, "Successfully processed step goal");
						}
						catch (Exception e)
						{
							Log.w(TAG, "Failed to process step goal", e);

							showGoalPrompt(forceMax);
						}
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						if (forceMax)
						{
							MainActivity.this.goalService.setCurrentGoal(Integer.MAX_VALUE);
							MainActivity.this.stepsGoalText.setText("Your Step Goal: ---");
						}
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
		calendar.add(Calendar.WEEK_OF_YEAR, -1);
		final long sundayTime = calendar.getTimeInMillis();

		final long minTime = Math.min(sundayTime, currentTime);
		final long maxTime = Math.max(sundayTime, currentTime);
		this.fitnessService.getFitnessSnapshots(minTime, maxTime)
				.onResult(new Consumer<Iterable<IFitnessSnapshot>>()
				{
					@Override
					public void accept(final Iterable<IFitnessSnapshot> incidentalSnapshots)
					{
						if (incidentalSnapshots == null)
							throw new IllegalStateException("Cannot find valid incidental fitness snapshots");

						activity.fitnessService.getRecordingSnapshots(minTime, maxTime)
								.onResult(new Consumer<Iterable<IFitnessSnapshot>>()
								{
									@Override
									public void accept(final Iterable<IFitnessSnapshot> intentionalSnapshots)
									{
										if (intentionalSnapshots == null)
											throw new IllegalStateException("Cannot find valid intentional fitness snapshots");

										activity.goalService.getGoalSnapshots(minTime, maxTime)
												.onResult(new Consumer<Iterable<IGoalSnapshot>>()
												{
													@Override
													public void accept(Iterable<IGoalSnapshot> iGoalSnapshots)
													{
														if (iGoalSnapshots == null)
															throw new IllegalStateException("Cannot find valid step goals");

														final Intent intent = new Intent(activity, GraphActivity.class);
														final Bundle weeklyBundle = activity.buildWeeklyBundle(incidentalSnapshots, intentionalSnapshots, iGoalSnapshots);

														final Bundle bundle = new Bundle();
														bundle.putBundle(GraphActivity.BUNDLE_WEEKLY_STATS, weeklyBundle);
														intent.putExtras(bundle);

														activity.startActivity(intent);
													}
												});
									}
								});
					}
				});
	}

	private Bundle buildWeeklyBundle(Iterable<IFitnessSnapshot> incidentalSnapshots, Iterable<IFitnessSnapshot> intentionalSnapshots, Iterable<IGoalSnapshot> stepGoals)
	{
		final Iterator<IFitnessSnapshot> incidentalIterator = incidentalSnapshots.iterator();
		final Iterator<IFitnessSnapshot> intentionalIterator = intentionalSnapshots.iterator();
		final Iterator<IGoalSnapshot> stepGoalIterator = stepGoals.iterator();

		final Bundle result = new Bundle();

		int dayCount = 0;
		while (dayCount < GraphActivity.BUNDLE_WEEK_LENGTH)
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

	private void displayRecordingSnapshot(IFitnessSnapshot fitnessSnapshot)
	{
		int steps = fitnessSnapshot.getTotalSteps();
		double duration = (fitnessSnapshot.getStopTime() - fitnessSnapshot.getStartTime()) / 1000.0;
		double speed = (steps) / (Math.min(duration, 1));

		Log.d(TAG, steps + " > " + duration + "s > " + speed + "fps");

		//activity.totalRunStepsText.setText(steps + " steps");
		this.currStepsText.setText(steps + " steps");
		this.timeElapsedText.setText(duration + " s");
		this.mphText.setText(speed + " fps");
	}
}
