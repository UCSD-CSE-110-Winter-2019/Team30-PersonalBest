package team30.personalbest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;

import java.util.Calendar;
import java.util.Iterator;

import team30.personalbest.framework.clock.FitnessClock;
import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.google.GoogleFitnessAdapter;
import team30.personalbest.framework.google.IGoogleService;
import team30.personalbest.framework.google.achiever.FitnessGoalAchiever;
import team30.personalbest.framework.service.IGoalService;
import team30.personalbest.framework.snapshot.IFitnessSnapshot;
import team30.personalbest.framework.snapshot.IGoalSnapshot;
import team30.personalbest.framework.snapshot.IRecordingFitnessSnapshot;
import team30.personalbest.framework.user.GoogleFitnessUser;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.framework.watcher.FitnessWatcher;
import team30.personalbest.util.Callback;

public class MainActivity extends AppCompatActivity
{
	public static final String TAG = "MainActivity";

	public static final int DEFAULT_GOAL_VALUE = 500;

	private GoogleFitnessAdapter googleFitnessAdapter;
	private GoogleFitnessUser currentUser;
	private FitnessClock currentClock;

	private FitnessWatcher fitnessWatcher;
	private FitnessGoalAchiever goalAchiever;

//TODO: remove this later
	public static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/*
		final FitnessOptions fitnessOptions = FitnessOptions.builder()
				.addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
				.addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
				.build();

		if (!GoogleSignIn.hasPermissions(
				GoogleSignIn.getLastSignedInAccount(this),
				fitnessOptions))
		{
			GoogleSignIn.requestPermissions(
					this,
					REQUEST_OAUTH_REQUEST_CODE,
					GoogleSignIn.getLastSignedInAccount(this),
					fitnessOptions
			);
		}
		else
		{
			updateStepCount();
			startRecording();
		}
		*/

		this.googleFitnessAdapter = new GoogleFitnessAdapter();
		this.currentClock = new FitnessClock();
		this.currentUser = new GoogleFitnessUser(this.googleFitnessAdapter);

		this.fitnessWatcher = new FitnessWatcher(this.currentUser, this.currentClock);
		this.fitnessWatcher.addFitnessListener(this::onFitnessUpdate);

		this.goalAchiever = new FitnessGoalAchiever(this.currentUser.getGoalService());
		this.goalAchiever.addGoalListener(this::onGoalAchievement);
		this.fitnessWatcher.addFitnessListener(this.goalAchiever);

		this.googleFitnessAdapter
				.addGoogleService(this.fitnessWatcher)
				.addGoogleService(this::onGoogleFitnessReady);

		this.setupUI();

		this.googleFitnessAdapter.onActivityCreate(this, savedInstanceState);
	}

	private void startRecording()
	{
		GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);

		if (googleSignInAccount == null) return;

		Log.d(TAG, "start recording");
		Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
				.subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE);
	}

	private void updateStepCount()
	{
		GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);

		Log.d(TAG, "Trying to update?");
		if (googleSignInAccount == null) return;

		Log.d(TAG, "WHAT?");
		Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
				.readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
				.addOnSuccessListener(dataSet ->
					Log.d(TAG, dataSet.toString())
				);
	}

	private void setupUI()
	{
		Button startWalk = findViewById(R.id.btn_walk_start);
		Button stopWalk = findViewById(R.id.btn_walk_stop);
		Button newGoal = findViewById(R.id.btn_stepgoal_new);
		Button weeklyStats = findViewById(R.id.btn_weekly_stats);

		//Disable screen (until initialized)...
		this.disableScreen();

		//Will only show during run
		findViewById(R.id.recordstat_steps).setVisibility(View.INVISIBLE);
		findViewById(R.id.recordstat_time).setVisibility(View.INVISIBLE);
		findViewById(R.id.recordstat_speed).setVisibility(View.INVISIBLE);
		startWalk.setVisibility(View.VISIBLE);
		stopWalk.setVisibility(View.GONE);

		startWalk.setOnClickListener(v -> this.startRecordingWalk());
		stopWalk.setOnClickListener(v -> this.stopRecordingWalk());
		newGoal.setOnClickListener(v -> this.showGoalPrompt(false));
		weeklyStats.setOnClickListener(v -> this.launchGraphActivity());
	}

	private void enableScreen()
	{
		findViewById(R.id.btn_walk_start).setEnabled(true);
		findViewById(R.id.btn_walk_stop).setEnabled(true);
		findViewById(R.id.btn_stepgoal_new).setEnabled(true);
		findViewById(R.id.btn_weekly_stats).setEnabled(true);
	}

	private void disableScreen()
	{
		findViewById(R.id.btn_walk_start).setEnabled(false);
		findViewById(R.id.btn_walk_stop).setEnabled(false);
		findViewById(R.id.btn_stepgoal_new).setEnabled(false);
		findViewById(R.id.btn_weekly_stats).setEnabled(false);
	}

	private void startRecordingWalk()
	{
		findViewById(R.id.btn_walk_start).setVisibility(View.GONE);
		findViewById(R.id.btn_walk_stop).setVisibility(View.VISIBLE);

		TextView recordStatSteps = findViewById(R.id.recordstat_steps);
		TextView recordStatTime = findViewById(R.id.recordstat_time);
		TextView recordStatSpeed = findViewById(R.id.recordstat_speed);

		recordStatSteps.setVisibility(View.VISIBLE);
		recordStatTime.setVisibility(View.VISIBLE);
		recordStatSpeed.setVisibility(View.VISIBLE);

		// Start recording current run
		IRecordingFitnessSnapshot snapshot = this.currentUser.getRecordingService()
				.startRecording(this.currentUser, this.currentClock)
				.addOnRecordingSnapshotUpdateListener(this::displayRecordingSnapshot);

		this.displayRecordingSnapshot(snapshot);
	}

	private void stopRecordingWalk()
	{
		findViewById(R.id.btn_walk_start).setVisibility(View.VISIBLE);
		findViewById(R.id.btn_walk_stop).setVisibility(View.GONE);

		//Stop and display just recorded walk stats
		this.currentUser.getRecordingService()
				.stopRecording(this.currentUser, this.currentClock)
				.onResult(this::displayRecordingSnapshot);
	}

	private void showGoalPrompt(boolean forceMax)
	{
		GoalPrompt.show(this, this.currentUser.getGoalService(), this.currentUser, this.currentClock, forceMax).onResult(newGoal -> {
			if (newGoal == null)
			{
				//Do nothing.
			}
			else if (newGoal == Integer.MAX_VALUE)
			{
				((TextView) findViewById(R.id.display_stepgoal)).setText(
						this.getString(R.string.display_stepgoal_none));
			}
			else
			{
				((TextView) findViewById(R.id.display_stepgoal)).setText(
						this.getString(R.string.display_stepgoal, newGoal));
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
	{
		/*
		if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_OAUTH_REQUEST_CODE)
		{
			updateStepCount();
			startRecording();
		}*/
		this.googleFitnessAdapter.onActivityResult(this, requestCode, resultCode, data);
	}

	public void onSubmitTime(View view)
	{
		TextView timeSubmitText = findViewById(R.id.timeText);
		String thisCurrTime = timeSubmitText.getText().toString();

		if (thisCurrTime.isEmpty())
		{
			Toast.makeText(this, "Please enter a valid time!", Toast.LENGTH_LONG).show();
		}

		else
		{
			long currentTime = Long.parseLong(thisCurrTime);

			//this.encouragement(currentTime);
			this.currentClock.freezeTimeAt(currentTime);
		}
	}

	protected Callback<IGoogleService> onGoogleFitnessReady(GoogleFitnessAdapter googleFitnessAdapter)
	{
		final Callback<IGoogleService> callback = new Callback<>(null);
		{
			final MainActivity activity = this;

			this.enableScreen();

			// Prompt height on initial launch of app (after google fit is ready)
			this.resolveHeight().onResult(aFloat -> {
				if (aFloat == null) throw new IllegalStateException("Unable to resolve height");

				((TextView) findViewById(R.id.display_height)).setText(activity.getString(R.string.display_height, aFloat));

				this.currentUser.getCurrentGoalSnapshot(this.currentClock).onResult(iGoalSnapshot -> {
					if (iGoalSnapshot == null)
					{
						((TextView) findViewById(R.id.display_stepgoal))
								.setText(activity.getString(R.string.display_stepgoal, DEFAULT_GOAL_VALUE));
					}
					else
					{
						((TextView) findViewById(R.id.display_stepgoal))
								.setText(activity.getString(R.string.display_stepgoal, iGoalSnapshot.getGoalValue()));
					}
				});

				this.currentUser.getCurrentDailySteps(this.currentClock)
						.onResult(integer -> ((TextView) findViewById(R.id.display_steptotal))
								.setText(activity.getString(R.string.display_steptotal, integer)));

				Log.i(TAG, "Successfully initialized app services");
			});

			Log.i(TAG, "Successfully prepared app services");
		}
		return callback;
	}

	protected void onFitnessUpdate(IFitnessUser user, IFitnessClock clock, Integer totalSteps)
	{
		if (totalSteps != null)
		{
			((TextView) this.findViewById(R.id.display_steptotal))
					.setText(this.getString(R.string.display_steptotal, totalSteps));
		}
		else
		{
			Log.d(TAG, "No steps found.");
		}
	}

	protected void onGoalAchievement(IGoalService goal)
	{
		//Achieved Goal!
		Toast.makeText(this, "Achieved step goal! Good job!", Toast.LENGTH_SHORT).show();
		showGoalPrompt(true);
	}

	private void displayRecordingSnapshot(IFitnessSnapshot iFitnessSnapshot)
	{
		if (iFitnessSnapshot != null)
		{
			//Display the stats!
			int steps = iFitnessSnapshot.getTotalSteps();
			double duration = (iFitnessSnapshot.getStopTime() - iFitnessSnapshot.getStartTime()) / 1000.0;
			double speed = (steps) / (Math.max(duration, 1));

			Log.d(TAG, steps + " > " + duration + "s > " + speed + "fps");

			TextView recordSteps = findViewById(R.id.recordstat_steps);
			TextView recordTime = findViewById(R.id.recordstat_time);
			TextView recordSpeed = findViewById(R.id.recordstat_speed);

			recordSteps.setText(this.getString(R.string.display_record_steps, steps));
			recordTime.setText(this.getString(R.string.display_record_time, duration));
			recordSpeed.setText(this.getString(R.string.display_record_speed, speed));
		}
	}

	private Callback<Float> resolveHeight()
	{
		final Callback<Float> callback = new Callback<>();
		{
			this.currentUser.getHeight(this.currentClock).onResult(aFloat -> {
				Log.d(TAG, "Resolving height...");
				if (aFloat == null)
				{
					HeightPrompt.show(
							this,
							this.currentUser.getHeightService(),
							this.currentUser,
							this.currentClock)
							.onResult(callback::resolve);
				}
				else
				{
					callback.resolve(aFloat);
				}
			});
		}
		return callback;
	}


	private void launchGraphActivity()
	{
		final MainActivity activity = this;

		final long currentTime = this.currentClock.getCurrentTime();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(currentTime);
		calendar.add(Calendar.WEEK_OF_YEAR, -1);
		final long sundayTime = calendar.getTimeInMillis();

		final long minTime = Math.min(sundayTime, currentTime);
		final long maxTime = Math.max(sundayTime, currentTime);
		final IFitnessClock clock = this.currentClock;
		this.currentUser.getFitnessSnapshots(clock, minTime, maxTime)
				.onResult(iFitnessSnapshots -> {
					if (iFitnessSnapshots == null)
						throw new IllegalStateException("Cannot find valid fitness snapshots");

					this.currentUser.getGoalSnapshots(clock, minTime, maxTime)
							.onResult(iGoalSnapshots -> {
								if (iGoalSnapshots == null)
									throw new IllegalStateException("Cannot find valid step goals");

								final Intent intent = new Intent(activity, GraphActivity.class);
								final Bundle weeklyBundle = activity.buildWeeklyBundle(iFitnessSnapshots, iGoalSnapshots);

								final Bundle bundle = new Bundle();
								bundle.putBundle(GraphActivity.BUNDLE_WEEKLY_STATS, weeklyBundle);
								intent.putExtras(bundle);

								activity.startActivity(intent);

							});
				});
	}

	private Bundle buildWeeklyBundle(Iterable<IFitnessSnapshot> fitnessSnapshots, Iterable<IGoalSnapshot> stepGoals)
	{
		final Iterator<IFitnessSnapshot> fitnessIterator = fitnessSnapshots.iterator();
		final Iterator<IGoalSnapshot> stepGoalIterator = stepGoals.iterator();

		final Bundle result = new Bundle();

		int dayCount = 0;
		while (dayCount < GraphActivity.BUNDLE_WEEK_LENGTH)
		{
			final Bundle dailyBundle = new Bundle();

			if (fitnessIterator.hasNext())
			{
				IFitnessSnapshot snapshot = fitnessIterator.next();
				dailyBundle.putInt(GraphActivity.BUNDLE_DAILY_STEPS,
				                   snapshot.getTotalSteps());
				dailyBundle.putInt(GraphActivity.BUNDLE_DAILY_ACTIVE_STEPS,
				                   snapshot.getRecordedSteps());
				dailyBundle.putLong(GraphActivity.BUNDLE_DAILY_TIMES,
				                    snapshot.getStopTime() - snapshot.getStartTime());
				dailyBundle.putDouble(GraphActivity.BUNDLE_DAILY_MPH,
				                      snapshot.getSpeed());
			}

			if (stepGoalIterator.hasNext())
			{
				IGoalSnapshot snapshot = stepGoalIterator.next();
				dailyBundle.putInt(GraphActivity.BUNDLE_DAILY_GOALS,
				                   snapshot.getGoalValue());
			}

			//Insert into result
			result.putBundle(GraphActivity.BUNDLE_WEEKLY_PREFIX + dayCount, dailyBundle);
			++dayCount;
		}

		return result;
	}
}
