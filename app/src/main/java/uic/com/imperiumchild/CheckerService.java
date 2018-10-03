package uic.com.imperiumchild;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.instantapps.ActivityCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.intentfilter.androidpermissions.PermissionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static java.util.Collections.singleton;

public class CheckerService extends Service {

    public int counter=0;
    private FirebaseAuth auth;
    FirebaseUser current;
    String value = "";
    String user = "";
    String passval = "";
    String useremail;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String TAG = "LocationMonitoring";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 10000;
    private static final float LOCATION_DISTANCE = 0;

    public CheckerService(Context applicationContext) {
        super();
    }

    public CheckerService(){

    }

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);

        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            String split[] = location.toString().split(" ");
            System.out.println("My Location: "+split[1]);
            DatabaseReference getuser = FirebaseDatabase.getInstance().getReference().child("Children");
            getuser.child(useremail).child("CurrentLocation").setValue(split[1]);

        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        auth = FirebaseAuth.getInstance();
        current = auth.getCurrentUser();
        passval = current.getEmail();
        String splitting[] = passval.split("@");
        useremail = splitting[0];
        getCurrentUser();
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

    @Override
    public void onCreate()
    {
        PermissionManager permissionManager = PermissionManager.getInstance(getApplicationContext());
        permissionManager.checkPermissions(singleton(android.Manifest.permission.ACCESS_FINE_LOCATION), new PermissionManager.PermissionRequestListener(){
            @Override
            public void onPermissionGranted() {
                Toast.makeText(getApplication(), "Permissions Granted", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onCreate");
                initializeLocationManager();
                try {
                    mLocationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                            mLocationListeners[1]);
                } catch (java.lang.SecurityException ex) {
                    Log.i(TAG, "Failed to request Location Update, ignore", ex);
                } catch (IllegalArgumentException ex) {
                    Log.d(TAG, "Network Provider does not exist, " + ex.getMessage());
                }
                try {
                    mLocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                            mLocationListeners[0]);
                } catch (java.lang.SecurityException ex) {
                    Log.i(TAG, "Failed to request Location Update, ignore", ex);
                } catch (IllegalArgumentException ex) {
                    Log.d(TAG, "GPS Provider does not exist " + ex.getMessage());
                }
            }

            @Override
            public void onPermissionDenied() {
                Toast.makeText(getApplicationContext(), "Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
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
        timer.schedule(timerTask1, 1000, 60000);
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

            System.out.println("\n"+appname + "\t" + pname);

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
        List<String> data = new ArrayList<>();
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
                String split[] = passval.split("@");
                data.add(newInfo.appname);

            }

        }

        String splits[] = passval.split("@");
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users");
        for(String datum : data) {
            rootRef.child(user).child("Children").child(splits[0]).child("Apps").child(datum).setValue(true);
        }

        return res;
    }

    public void getCurrentUser(){

        String split[] = passval.split("@");

        DatabaseReference getuser = FirebaseDatabase.getInstance().getReference().child("Children");
        getuser.child(split[0]).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if( dataSnapshot != null){

                    user = dataSnapshot.getValue(String.class);
                    System.out.println(user);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {



            }
        });

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

