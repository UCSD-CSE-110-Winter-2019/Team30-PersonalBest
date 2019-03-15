package team30.personalbest.framework.watcher;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.google.GoogleFitnessAdapter;
import team30.personalbest.framework.google.IGoogleService;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.util.Callback;

public class FitnessWatcher implements IGoogleService
{
	public static final String TAG = "FitnessWatcher";

	public static final int WATCH_SAMPLE_RATE = 10000;

	private final List<OnStepUpdateListener> listeners = new ArrayList<>();
	private final IFitnessUser fitnessUser;
	private final IFitnessClock clock;

	private WatcherTask watchTask;

	public FitnessWatcher(IFitnessUser fitnessUser, IFitnessClock clock)
	{
		this.fitnessUser = fitnessUser;
		this.clock = clock;
	}

	@Override
	public Callback<FitnessWatcher> initialize(GoogleFitnessAdapter googleFitnessAdapter)
	{
		Callback<FitnessWatcher> callback = new Callback<>();
		{
			this.start();
			callback.resolve(this);
		}
		return callback;
	}

	public FitnessWatcher addFitnessListener(OnStepUpdateListener listener)
	{
		this.listeners.add(listener);
		return this;
	}

	public void removeFitnessListener(OnStepUpdateListener listener)
	{
		this.listeners.remove(listener);
	}

	public void start()
	{
		this.watchTask = new WatcherTask(this);
		this.watchTask.execute();
	}

	public void stop()
	{
		this.watchTask.cancel(true);
		this.watchTask = null;
	}

	public void update()
	{
		this.fitnessUser.getCurrentDailySteps(this.clock).onResult(integer -> {
			for (OnStepUpdateListener listener : this.listeners)
			{
				listener.onStepUpdate(this.fitnessUser, this.clock, integer);
			}
		});
	}

	private static final class WatcherTask extends AsyncTask<Void, Void, Void>
	{
		private final FitnessWatcher watcher;

		WatcherTask(FitnessWatcher watcher)
		{
			this.watcher = watcher;
		}

		@Override
		protected void onProgressUpdate(Void... values)
		{
			super.onProgressUpdate(values);

			this.watcher.update();
		}

		@Override
		protected Void doInBackground(Void... voids)
		{
			try
			{
				while (true)
				{
					Thread.sleep(WATCH_SAMPLE_RATE);

					Log.d(TAG, "...watching your fitness data...");
					this.publishProgress();
				}
			}
			catch (Exception e)
			{
				Log.w(TAG, "Failed to update fitness watcher.", e);
			}
			return null;
		}
	}
}
