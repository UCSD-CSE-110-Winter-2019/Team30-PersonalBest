package team30.personalbest;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.fitness.data.Goal;

import team30.personalbest.framework.clock.FitnessClock;
import team30.personalbest.framework.clock.IFitnessClock;
import team30.personalbest.framework.google.GoogleFitnessAdapter;
import team30.personalbest.framework.google.IGoogleService;
import team30.personalbest.framework.google.achiever.FitnessGoalAchiever;
import team30.personalbest.framework.service.IGoalService;
import team30.personalbest.framework.user.GoogleFitnessUser;
import team30.personalbest.framework.user.IFitnessUser;
import team30.personalbest.framework.watcher.FitnessWatcher;
import team30.personalbest.util.Callback;

public class GoalNotifyService extends Service {
    public static String TAG = "GoalNotifyService";
    public static String CHANNEL_ID = "GoalNotifyChannel";

    public static GoogleFitnessUser LOCAL_USER;
    public static FitnessClock LOCAL_CLOCK;

    private GoogleFitnessAdapter googleFitnessAdapter;
    private GoogleFitnessUser currentUser;
    private FitnessClock currentClock;

    private FitnessWatcher fitnessWatcher;
    private FitnessGoalAchiever goalAchiever;

    private int notificationId = 0xffff;

    public GoalNotifyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(GoalNotifyService.this, "GoalNotify Service Started", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "goal notify service started");

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


        //TODO: can' figure out how
        //      plus there is the problem of this service outliving mainActivity,
        //      so I'm not sure how to deal with that
        //this.googleFitnessAdapter.onActivityCreate(mainActivity, null);

        createNotificationChannel();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        Toast.makeText(GoalNotifyService.this, "GoalNotify Service Stopped", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "goal notify service destroyed");
        super.onDestroy();
    }

    protected void onFitnessUpdate(IFitnessUser user, IFitnessClock clock, Integer totalSteps)
    {
        if (totalSteps != null)
        {
            //NOTE: do nothing
        }
        else
        {
            Log.d(TAG, "No steps found.");
        }
    }

    protected void onGoalAchievement(IGoalService goal)
    {
        Log.d(TAG, "GoalNotifyService is notified with goal achievement");

        //TODO: push notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Achieved Goal")
                .setContentText("You have achieved your step goal!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, builder.build());
        notificationId += 1;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "goalNotify";
            String description = "channel for notifying achieve goal";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    protected Callback<IGoogleService> onGoogleFitnessReady(GoogleFitnessAdapter googleFitnessAdapter)
    {
        //NOTE: stub, do nothing here
        final Callback<IGoogleService> callback = new Callback<>(null);
        return callback;
    }
}
