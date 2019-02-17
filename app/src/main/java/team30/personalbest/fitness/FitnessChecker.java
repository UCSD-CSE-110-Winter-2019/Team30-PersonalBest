package team30.personalbest.fitness;

import android.os.AsyncTask;
import android.support.v4.util.Consumer;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import team30.personalbest.fitness.service.IFitnessService;
import team30.personalbest.fitness.snapshot.IFitnessSnapshot;

public class FitnessChecker
{
    public static final String TAG = "FitnessChecker";
    public static final int TIME_UPDATE_MILLIS = 10000;

    private final List<IFitnessUpdateListener> listeners = new ArrayList<>();
    private final IFitnessService fitnessService;
    private FitnessCheckTask checkTask;
    private volatile boolean running = false;

    public FitnessChecker(IFitnessService fitnessService)
    {
        this.fitnessService = fitnessService;
    }

    public FitnessChecker addListener(IFitnessUpdateListener listener)
    {
        this.listeners.add(listener);
        return this;
    }

    public void removeListener(IFitnessUpdateListener listener)
    {
        this.listeners.remove(listener);
    }

    public Iterable<IFitnessUpdateListener> getListeners()
    {
        return this.listeners;
    }

    public void startChecking()
    {
        if (this.checkTask != null && !this.checkTask.isCancelled())
        {
            this.checkTask.cancel(true);
        }

        this.running = true;
        this.checkTask = new FitnessCheckTask(this);
        this.checkTask.execute();
    }

    public void stopChecking()
    {
        if (!this.checkTask.isCancelled())
        {
            this.checkTask.cancel(true);
        }
    }

    private void onFitnessUpdate(IFitnessSnapshot fitnessSnapshot)
    {
        if (fitnessSnapshot == null)
        {
            Log.e(TAG, "Unable to find fitness snapshot for update");
        }

        for(IFitnessUpdateListener listener : this.listeners)
        {
            listener.onStepUpdate(fitnessSnapshot);
        }
    }

    public boolean isRunning()
    {
        return this.running;
    }

    public IFitnessService getFitnessService()
    {
        return this.fitnessService;
    }

    private static final class FitnessCheckTask extends AsyncTask<Void, Void, Void>
    {
        private final FitnessChecker checker;

        FitnessCheckTask(FitnessChecker checker)
        {
            this.checker = checker;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);

            this.checker.getFitnessService().getFitnessSnapshot().onResult(new Consumer<IFitnessSnapshot>() {
                @Override
                public void accept(IFitnessSnapshot iFitnessSnapshot) {
                    FitnessCheckTask.this.checker.onFitnessUpdate(iFitnessSnapshot);
                    FitnessCheckTask.this.checker.startChecking();
                }
            });
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            try
            {
                Thread.sleep(TIME_UPDATE_MILLIS);
            }
            catch(Exception e)
            {
                Log.e(TAG, "Failed to update fitness check", e);
            }

            return null;
        }
    }
}
