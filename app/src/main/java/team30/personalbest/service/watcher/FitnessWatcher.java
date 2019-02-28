package team30.personalbest.service.watcher;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Consumer;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import team30.personalbest.service.IService;
import team30.personalbest.service.ServiceInitializer;
import team30.personalbest.service.fitness.IFitnessService;
import team30.personalbest.snapshot.IFitnessSnapshot;
import team30.personalbest.util.Callback;

public class FitnessWatcher implements IService
{
	public static final String TAG = "FitnessWatcher";

	public static final int WATCH_SAMPLE_RATE = 10000;

	private final List<OnFitnessUpdateListener> listeners = new ArrayList<>();
	private final IFitnessService fitnessService;

	private WatcherTask watchTask;

	public FitnessWatcher(IFitnessService fitnessService)
	{
		this.fitnessService = fitnessService;
	}

	public FitnessWatcher addFitnessListener(OnFitnessUpdateListener listener)
	{
		this.listeners.add(listener);
		return this;
	}

	public void removeFitnessListener(OnFitnessUpdateListener listener)
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
		this.fitnessService.getFitnessSnapshot().onResult(new Consumer<IFitnessSnapshot>()
		{
			@Override
			public void accept(IFitnessSnapshot fitnessSnapshot)
			{
				for (OnFitnessUpdateListener listener : FitnessWatcher.this.listeners)
				{
					listener.onFitnessUpdate(fitnessSnapshot);
				}
			}
		});
	}

	public IFitnessService getFitnessService()
	{
		return this.fitnessService;
	}

	@Nullable
	@Override
	public Callback<IService> onActivityCreate(ServiceInitializer serviceInitializer, Activity activity, Bundle savedInstanceState)
	{
		this.start();
		return new Callback<>(this);
	}

	@Nullable
	@Override
	public Callback<IService> onActivityResult(ServiceInitializer serviceInitializer, Activity activity, int requestCode, int resultCode, @Nullable Intent data)
	{
		return null;
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
