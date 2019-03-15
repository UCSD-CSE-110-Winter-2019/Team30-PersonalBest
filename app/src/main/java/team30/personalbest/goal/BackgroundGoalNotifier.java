package team30.personalbest.goal;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.util.Consumer;
import android.util.Log;

import androidx.annotation.RequiresApi;
import team30.personalbest.service.fitness.IFitnessService;
import team30.personalbest.service.goal.IGoalService;
import team30.personalbest.snapshot.IFitnessSnapshot;
import team30.personalbest.snapshot.IGoalSnapshot;

public class BackgroundGoalNotifier extends IntentService {
    public static String TAG = "BackgroundGoalNotifier";
    private boolean goalHasBeenAchieved;

    public BackgroundGoalNotifier() {
        super("BackgroundGoalNotifier");
        goalHasBeenAchieved = false;

        Log.d(TAG, "started background goal notifier service");
    }

    @Override
    protected void onHandleIntent(Intent workIntent)
    {
        IFitnessService fitnessService = (IFitnessService) workIntent.getExtras().get("fitnessService");
        IGoalService goalService = (IGoalService) workIntent.getExtras().get("goalService");

        if (fitnessService == null || goalService == null)
        {
            Log.d(TAG, "failed to retrieve fitnessService and goalService");
            return;
        }

        while (!goalHasBeenAchieved)
        {
            // check if goal is reached
            fitnessService.getFitnessSnapshot().onResult(new Consumer<IFitnessSnapshot>()
            {
                @Override
                public void accept(IFitnessSnapshot fitnessSnapshot)
                {
                    if (fitnessSnapshot != null)
                    {
                        goalService.getGoalSnapshot().onResult(new Consumer<IGoalSnapshot>() {
                            @Override
                            public void accept(IGoalSnapshot iGoalSnapshot)
                            {
                                if (iGoalSnapshot != null)
                                {
                                    Log.d(TAG, "Trying to achieve goal " + iGoalSnapshot.getGoalValue() + " for " + fitnessSnapshot.getTotalSteps() + "...");
                                    //if (fitnessSnapshot.getTotalSteps() >= iGoalSnapshot.getGoalValue())
                                    {
                                        goalHasBeenAchieved = true;
                                        pushGoalAchieveNotification();
                                    }
                                }
                            }
                        });
                    }
                }
            });

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // reference: https://www.tutorialspoint.com/android/android_push_notification.htm
    private void pushGoalAchieveNotification()
    {
        Log.d(TAG, "pushing goal achieved notification");

        /*
        String title = "Reached Goal";
        String body = "You have reached your step goal for today!";
        String subject = "Personal Best";

        NotificationManager notif = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notify= new Notification.Builder
                (getApplicationContext()).setContentTitle(title).setContentText(body).
                setContentTitle(subject).build();

        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        notif.notify(0, notify);
        */
    }
}
