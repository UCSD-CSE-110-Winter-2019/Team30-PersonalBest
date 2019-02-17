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
    private final FitnessCheckTask checkTask;
    private volatile boolean running = false;

    public FitnessChecker(IFitnessService fitnessService)
    {
        this.fitnessService = fitnessService;
        this.checkTask = new FitnessCheckTask(this);
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
        if (!this.checkTask.isCancelled())
        {
            this.checkTask.cancel(true);
        }

        this.checkTask.execute();
    }

    public void stopChecking()
    {
        if (!this.checkTask.isCancelled())
        {
            this.checkTask.cancel(true);
        }
    }

    public void onFitnessUpdate(IFitnessSnapshot fitnessSnapshot)
    {
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

    private final class FitnessCheckTask extends AsyncTask<Void, Void, Void>
    {
        private final FitnessChecker checker;

        FitnessCheckTask(FitnessChecker checker)
        {
            this.checker = checker;
        }

        @Override
        protected Void doInBackground( Void... voids)
        {
            try
            {
                while(this.checker.isRunning())
                {
                    Thread.sleep(TIME_UPDATE_MILLIS);
                    this.checker.getFitnessService().getFitnessSnapshot().onResult(new Consumer<IFitnessSnapshot>() {
                        @Override
                        public void accept(IFitnessSnapshot iFitnessSnapshot) {
                            FitnessCheckTask.this.checker.onFitnessUpdate(iFitnessSnapshot);
                        }
                    });
                }
            }
            catch(Exception e)
            {
                Log.e(TAG, "Failed to update fitness check", e);
            }
            return voids[0];
        }
    }
}
