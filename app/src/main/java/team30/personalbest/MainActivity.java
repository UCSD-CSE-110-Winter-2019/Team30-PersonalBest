package team30.personalbest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import team30.personalbest.framework.clock.FitnessClock;
import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.google.GoogleFitnessAdapter;
import team30.personalbest.framework.google.IGoogleService;
import team30.personalbest.framework.google.achiever.FitnessGoalAchiever;
import team30.personalbest.framework.service.IGoalService;
import team30.personalbest.framework.snapshot.IFitnessSnapshot;
import team30.personalbest.framework.snapshot.IRecordingFitnessSnapshot;
import team30.personalbest.framework.user.GoogleFitnessUser;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.framework.watcher.FitnessWatcher;
import team30.personalbest.messeging.MessageActivity;
import team30.personalbest.util.Callback;

public class MainActivity extends AppCompatActivity
{
	public static final String TAG = "MainActivity";

	public static final int DEFAULT_GOAL_VALUE = 500;
	public static GoogleFitnessUser LOCAL_USER;
	public static FitnessClock LOCAL_CLOCK;

	private GoogleFitnessAdapter googleFitnessAdapter;
	private GoogleFitnessUser currentUser;
	private FitnessClock currentClock;

	private FitnessWatcher fitnessWatcher;
	private FitnessGoalAchiever goalAchiever;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.googleFitnessAdapter = new GoogleFitnessAdapter();
		this.currentClock = new FitnessClock();
		LOCAL_CLOCK = this.currentClock;
		this.currentUser = new GoogleFitnessUser(this.googleFitnessAdapter);
		LOCAL_USER = this.currentUser;

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

	private void setupUI()
	{
		Button startWalk = findViewById(R.id.btn_walk_start);
		Button stopWalk = findViewById(R.id.btn_walk_stop);
		Button newGoal = findViewById(R.id.btn_stepgoal_new);
		Button weeklyStats = findViewById(R.id.btn_weekly_stats);
		Button friendsList = findViewById(R.id.btn_friends);

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
		friendsList.setOnClickListener(v -> this.launchFriendsActivity());
	}

	private void enableScreen()
	{
		findViewById(R.id.btn_walk_start).setEnabled(true);
		findViewById(R.id.btn_walk_stop).setEnabled(true);
		findViewById(R.id.btn_stepgoal_new).setEnabled(true);
		findViewById(R.id.btn_weekly_stats).setEnabled(true);
		findViewById(R.id.btn_friends).setEnabled(true);
	}

	private void disableScreen()
	{
		findViewById(R.id.btn_walk_start).setEnabled(false);
		findViewById(R.id.btn_walk_stop).setEnabled(false);
		findViewById(R.id.btn_stepgoal_new).setEnabled(false);
		findViewById(R.id.btn_weekly_stats).setEnabled(false);
		findViewById(R.id.btn_friends).setEnabled(false);
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
			else if (newGoal >= Integer.MAX_VALUE)
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
						int goalValue = iGoalSnapshot.getGoalValue();
						if (goalValue >= Integer.MAX_VALUE)
						{
							((TextView) findViewById(R.id.display_stepgoal)).setText(
									this.getString(R.string.display_stepgoal_none));
						}
						else
						{
							((TextView) findViewById(R.id.display_stepgoal))
									.setText(activity.getString(R.string.display_stepgoal, goalValue));
						}
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
		GraphBundler.makeBundle(this.currentClock, this.currentUser).onResult(bundle -> {
			final Intent intent = new Intent(this, GraphActivity.class);
			intent.putExtras(bundle);
			this.startActivity(intent);
		});
	}

	private void launchFriendsActivity()
	{
		Intent intent = new Intent(this, MessageActivity.class);
		this.startActivity(intent);
	}
}
