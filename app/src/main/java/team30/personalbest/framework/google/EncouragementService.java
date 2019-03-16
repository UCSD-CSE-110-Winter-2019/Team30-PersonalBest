package team30.personalbest.framework.google;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Iterator;

import team30.personalbest.R;
import team30.personalbest.framework.IFitnessAdapter;
import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.service.IEncouragementService;
import team30.personalbest.framework.service.IFitnessService;
import team30.personalbest.framework.snapshot.IFitnessSnapshot;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.util.Callback;

public class EncouragementService implements IEncouragementService, IGoogleService
{
	public static final String TAG = "EncouragementService";

	public static final String SHARED_PREFS_NAME = "personalBest";
	public static final String SHARED_PREFS_ENCOURAGEMENT_TIME_KEY = "lastEncouragementTime";

	public static final float NEARLY_FACTOR = 1.9F;
	public static final float FULL_FACTOR = 2.0F;

	private final IFitnessService fitnessService;

	public EncouragementService(IFitnessService fitnessService)
	{
		this.fitnessService = fitnessService;
	}

	@Override
	public Callback<EncouragementService> initialize(IFitnessAdapter googleFitnessAdapter)
	{
		return new Callback<>(this);
	}

	public long getLastEncouragementTime(Context context)
	{
		final SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		return sharedPreferences.getLong(SHARED_PREFS_ENCOURAGEMENT_TIME_KEY, -1);
	}

	public void tryEncouragement(Context context, IFitnessUser user, IFitnessClock clock, long currentTime)
	{
		Calendar current = Calendar.getInstance();
		current.setTimeInMillis(currentTime);
		current.add(Calendar.DAY_OF_YEAR, -2);
		long previous2 = current.getTimeInMillis();

		this.fitnessService.getFitnessSnapshots(user, clock, previous2, currentTime).onResult(iFitnessSnapshots -> {
			Iterator<IFitnessSnapshot> iterator = iFitnessSnapshots.iterator();
			if (iterator.hasNext())
			{
				long prev2 = iterator.next().getTotalSteps();
				if (iterator.hasNext())
				{
					long prev1 = iterator.next().getTotalSteps();
					if (prev1 > prev2 * FULL_FACTOR)
					{
						showFullEncouragement(context);
					}
					else if (prev1 > prev2 * NEARLY_FACTOR)
					{
						showNearlyEncouragement(context);
					}
					else
					{
						//Don't show nothing.
					}
				}
			}
			else
			{
				Log.w(TAG, "Unable to find steps for yesterday!");
			}
		});
	}

	private void showFullEncouragement(Context context)
	{
		Toast.makeText(context, context.getString(R.string.encouragement_significant), Toast.LENGTH_LONG).show();
	}

	private void showNearlyEncouragement(Context context)
	{
		Toast.makeText(context, context.getString(R.string.encouragement_nearly_significant), Toast.LENGTH_LONG).show();
	}

	/*
	private void encouragement()
	{
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
	*/
}
