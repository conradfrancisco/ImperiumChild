package uic.com.imperiumchild;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CheckerService extends Service {

    public int counter=0;
    String value = "";
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public CheckerService(Context applicationContext) {
        super();
    }

    public CheckerService(){

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        startTimer();
        startTimer1();

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent("RestartSensor");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer, timer1;
    private TimerTask timerTask, timerTask1;
    long oldTime=0, oldTime1=0;
    public void startTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 1000, 1000);
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {

                notifs();



            }
        };
    }

    public void startTimer1() {
        timer = new Timer();
        initializeTimerTask1();
        timer.schedule(timerTask1, 60000, 60000);
    }

    public void initializeTimerTask1() {
        timerTask1 = new TimerTask() {
            public void run() {

                getPackages();



            }
        };
    }

    public void stoptimertask() {
        if ((timer != null) && (timer1 != null) ){
            timer.cancel();
            timer1.cancel();
            timer = null;
            timer1 = null;
        }
    }
    public void notifs(){

        IntentFilter ifl = new IntentFilter();
        ifl.addAction("ok");

        Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(""));
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pit = PendingIntent.getActivity(getApplicationContext(), 0, it, 0);
        Context con = getApplicationContext();

        Notification.Builder build;

            build = new Notification.Builder (con)
                    .setContentTitle("ImperiumMonitoring")
                    .setContentText("You are being monitored")
                    .setContentIntent(pit)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setOngoing(true);


        Notification notifs = build.build();

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notifs);
    }

    class PInfo {
        private String appname = "";
        private String pname = "";

        private void prettyPrint() {

            System.out.println(appname + "\t" + pname);

        }
    }

    private ArrayList<PInfo> getPackages() {
        ArrayList<PInfo> apps = getInstalledApps(false);
        final int max = apps.size();
        for (int i=0; i<max; i++) {
            apps.get(i).prettyPrint();
        }
        return apps;
    }

    private ArrayList<PInfo> getInstalledApps(boolean getSysPackages) {
        ArrayList<PInfo> res = new ArrayList<PInfo>();
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
        for(int i=0;i<packs.size();i++) {
            PackageInfo p = packs.get(i);
            if ((!getSysPackages) && (p.versionName == null)) {
                continue;
            }
            PInfo newInfo = new PInfo();
            newInfo.appname = p.applicationInfo.loadLabel(getPackageManager()).toString();
            newInfo.pname = p.packageName;

            if (newInfo.appname.equals("Facebook") || newInfo.appname.equals("Youtube") || newInfo.appname.equals("Twitter") || newInfo.appname.equals("Instagram") || newInfo.appname.equals("Chrome") || newInfo.appname.equals("Tumblr") || newInfo.appname.equals("Pinterest") || newInfo.appname.equals("Rise of Civilizations")){

                res.add(newInfo);
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users");

            }
        }
        return res;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

