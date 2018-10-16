package uic.com.imperiumchild;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.intentfilter.androidpermissions.PermissionManager;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import static java.util.Collections.singleton;

public class CheckerService extends Service {

    public int counter=0;
    private FirebaseAuth auth;
//    private String values = "";
//    private DatabaseReference ref;
    FirebaseUser current;
    String value = "";
    String user = "";
    String passval = "";
    String useremail;
    String splitss[];
    private int statusBlock;
    private int statusBlock1;
    private int statusBlock2;
//    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String TAG = "LocationMonitoring";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 10000;
    private static final float LOCATION_DISTANCE = 0;
    private BroadcastReceiver mReceiver, mReceiver4, mReceiver5 ;
    private int isBlocked;
    final Context ctx = this;

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
            System.out.println("Useremail: "+useremail+" and Email: "+splitss[0]);
            getuser.child(useremail).child(splitss[0]).child("CurrentLocation").setValue(split[1]);

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
        try{

            auth = FirebaseAuth.getInstance();
            current = auth.getCurrentUser();
            passval = current.getEmail();
            splitss = passval.split("@");
            getCurrentParentUser();
            startTimer();
            startTimer1();
            startTimer2();
            startTimer3();
            startTimer4();
            startTimer5();

        }

        catch(Exception e){

            Log.e("onStartCommandChecker", e.getMessage(), e);

        }

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

    private Timer timer, timer1, timer2, timer3, timer4, timer5;
    private TimerTask timerTask, timerTask1, timerTask2, timerTask3, timerTask4, timerTask5;
    long oldTime=0, oldTime1=0, oldTime2=0, oldTime3=0, oldTime4=0, oldTime5=0;
    public void startTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 5000, 5000);
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {

                notifs();
                getDeviceBlock();
                getStatus();
                getStatusW();
                getStatusD();


            }
        };
    }

    public void startTimer1() {
        timer1 = new Timer();
        initializeTimerTask1();
        timer1.schedule(timerTask1, 10000, 60000);
    }

    public void initializeTimerTask1() {
        timerTask1 = new TimerTask() {
            public void run() {

                getPackages();

            }
        };
    }

    public void startTimer2() {
        timer2 = new Timer();
        initializeTimerTask2();
        timer2.schedule(timerTask2, 7000, 5000);
    }

    public void startTimer3() {
        timer3 = new Timer();
        initializeTimerTask3();
        timer3.schedule(timerTask3, 7000, 5000);
    }

    public void initializeTimerTask2() {
        timerTask2 = new TimerTask() {
            public void run() {

                try{

                    if(isBlocked == 1) {

                        try{

                            OutputStreamWriter out = new OutputStreamWriter(ctx.openFileOutput("test.txt", ctx.MODE_PRIVATE));
                            out.write("1");
                            out.close();
                            KeyguardManager.KeyguardLock key;
                            KeyguardManager km = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
                            key = km.newKeyguardLock("IN");
                            key.disableKeyguard();
                            IntentFilter filter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
                            filter.addAction(Intent.ACTION_TIME_TICK);
                            mReceiver = new Broadcaster();
                            registerReceiver(mReceiver, filter);

                        }

                        catch(IOException e){

                            Log.e("Exception", "File write failed: " + e.toString());

                        }
                    }
                    else if(isBlocked == 0){

                        try{

                            OutputStreamWriter out = new OutputStreamWriter(ctx.openFileOutput("test.txt", ctx.MODE_PRIVATE));
                            out.write("0");
                            out.close();
                            HomeKeyLocker homeKeyLocker = new HomeKeyLocker();
                            homeKeyLocker.unlock();

                        }

                        catch(IOException e){

                            Log.e("Exception", "File write failed: " + e.toString());

                        }
                    }
                    else {

                        Log.d("InvalidNumber", "Invalid Number Retrieved");

                    }

                }

                catch(Exception e){

                    Log.e("onDeviceBlockPrimary", e.getMessage(), e);

                }
            }
        };
    }

    public void initializeTimerTask3() {
        timerTask3 = new TimerTask() {
            public void run() {

                try{

                    if(statusBlock == 1) {

                        OutputStreamWriter out = new OutputStreamWriter(ctx.openFileOutput("test1.txt", ctx.MODE_PRIVATE));
                        out.write("1");
                        out.close();
                        KeyguardManager.KeyguardLock key;
                        KeyguardManager km = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
                        key = km.newKeyguardLock("IN");
                        key.disableKeyguard();
                        IntentFilter filter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
                        filter.addAction(Intent.ACTION_TIME_TICK);
                        mReceiver = new StatusBroadcaster();
                        registerReceiver(mReceiver, filter);

                    }
                    else if(statusBlock == 0){

                        OutputStreamWriter out = new OutputStreamWriter(ctx.openFileOutput("test1.txt", ctx.MODE_PRIVATE));
                        out.write("0");
                        out.close();
                        HomeKeyLocker homeKeyLocker = new HomeKeyLocker();
                        homeKeyLocker.unlock();

                    }

                    else {

                        Log.d("InvalidNumber", "Invalid Number Retrieved");
                    }

                }

                catch(Exception e){

                    Log.e("onDeviceBlock", e.getMessage(), e);

                }
            }
        };
    }

    public void startTimer4() {
        timer4 = new Timer();
        initializeTimerTask4();
        timer4.schedule(timerTask4, 7000, 604800000);
    }

    public void initializeTimerTask4() {
        timerTask3 = new TimerTask() {
            public void run() {

                try{

                    if(statusBlock1 == 1) {

                        OutputStreamWriter out = new OutputStreamWriter(ctx.openFileOutput("test4.txt", ctx.MODE_PRIVATE));
                        out.write("1");
                        out.close();
                        KeyguardManager.KeyguardLock key;
                        KeyguardManager km = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
                        key = km.newKeyguardLock("IN");
                        key.disableKeyguard();
                        IntentFilter filter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
                        filter.addAction(Intent.ACTION_TIME_TICK);
                        mReceiver4 = new WStatusBroadCaster();
                        registerReceiver(mReceiver4, filter);

                    }
                    else if(statusBlock1 == 0){

                        OutputStreamWriter out = new OutputStreamWriter(ctx.openFileOutput("test4.txt", ctx.MODE_PRIVATE));
                        out.write("0");
                        out.close();
                        HomeKeyLocker homeKeyLocker = new HomeKeyLocker();
                        homeKeyLocker.unlock();

                    }

                    else {

                        Log.d("InvalidNumber", "Invalid Number Retrieved");
                    }

                }

                catch(Exception e){

                    Log.e("onDeviceBlock", e.getMessage(), e);

                }
            }
        };
    }

    public void startTimer5() {
        timer5 = new Timer();
        initializeTimerTask5();
        timer5.schedule(timerTask5, 7000, 86400000);
    }

    public void initializeTimerTask5() {
        timerTask3 = new TimerTask() {
            public void run() {

                try{

                    if(statusBlock2 == 1) {

                        OutputStreamWriter out = new OutputStreamWriter(ctx.openFileOutput("test5.txt", ctx.MODE_PRIVATE));
                        out.write("1");
                        out.close();
                        KeyguardManager.KeyguardLock key;
                        KeyguardManager km = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
                        key = km.newKeyguardLock("IN");
                        key.disableKeyguard();
                        IntentFilter filter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
                        filter.addAction(Intent.ACTION_TIME_TICK);
                        mReceiver4 = new WStatusBroadCaster();
                        registerReceiver(mReceiver4, filter);

                    }
                    else if(statusBlock2 == 0){

                        OutputStreamWriter out = new OutputStreamWriter(ctx.openFileOutput("test5.txt", ctx.MODE_PRIVATE));
                        out.write("0");
                        out.close();
                        HomeKeyLocker homeKeyLocker = new HomeKeyLocker();
                        homeKeyLocker.unlock();

                    }

                    else {

                        Log.d("InvalidNumber", "Invalid Number Retrieved");
                    }

                }

                catch(Exception e){

                    Log.e("onDeviceBlock", e.getMessage(), e);

                }
            }
        };
    }



    public void stoptimertask() {
        if ((timer != null) && (timer1 != null) && (timer2 != null) ){
            timer.cancel();
            timer1.cancel();
            timer2.cancel();
            timer3.cancel();
            timer = null;
            timer1 = null;
            timer2 = null;
            timer3 = null;
        }
    }

    public void getDeviceBlock(){

        try{

            DatabaseReference getdev = FirebaseDatabase.getInstance().getReference();
            getdev.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    try{

                        System.out.println("Current Parent User: "+useremail+" and Current Child User: "+splitss[0]);
                        String value = dataSnapshot.child("Users").child(useremail).child("Children").child(splitss[0]).child("BlockDevice").getValue(String.class);
                        if(value !=null){

                            int intval = Integer.parseInt(value);
                            System.out.println("I am currently: "+value);
                            if(intval == 0){

                                isBlocked = 0;
                            }

                            else {

                                isBlocked = 1;
                            }

                        }
                        else{

                            Log.d("DeviceBlock", "No Value has been RETRIEVED");

                        }

                    }

                    catch(Exception e){

                        Log.e("DeviceBlock", e.getMessage(), e);

                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        catch(Exception e){

            Log.e("onGetDeviceBlock", e.getMessage(), e);

        }

    }

    public void getStatus(){

        try{

            DatabaseReference getstat = FirebaseDatabase.getInstance().getReference();
            getstat.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    try{

                        System.out.println("Current Parent User: "+useremail+" and Current Child User: "+splitss[0]);
                        String value = dataSnapshot.child("Users").child(useremail).child("Children").child(splitss[0]).child("Tasks").child("Status").getValue(String.class);
                        if(value!=null && value.equals("1") || value.equals("0")){

                            statusBlock = Integer.parseInt(value);

                        }
                        else {

                            Log.d("OnGetStatus", "Invalid Status Number Retrieved");

                        }


                    }

                    catch(Exception e){

                        Log.e("StatusRetrieve", e.getMessage(), e);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        catch(Exception e){

            Log.e("onGetStatus", e.getMessage(), e);

        }
    }


    public void getStatusW(){

        try{

            DatabaseReference getstat = FirebaseDatabase.getInstance().getReference();
            getstat.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    try{

                        System.out.println("Current Parent User: "+useremail+" and Current Child User: "+splitss[0]);
                        String value = dataSnapshot.child("Users").child(useremail).child("Children").child(splitss[0]).child("WeeklyTasks").child("Status").getValue(String.class);
                        if(value!=null && value.equals("1") || value.equals("0")){

                            statusBlock1 = Integer.parseInt(value);

                        }
                        else {

                            Log.d("OnGetStatus", "Invalid Status Number Retrieved");

                        }


                    }

                    catch(Exception e){

                        Log.e("StatusRetrieve", e.getMessage(), e);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        catch(Exception e){

            Log.e("onGetStatus", e.getMessage(), e);

        }
    }

    public void getStatusD(){

        try{

            DatabaseReference getstat = FirebaseDatabase.getInstance().getReference();
            getstat.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    try{

                        System.out.println("Current Parent User: "+useremail+" and Current Child User: "+splitss[0]);
                        String value = dataSnapshot.child("Users").child(useremail).child("Children").child(splitss[0]).child("DailyTasks").child("Status").getValue(String.class);
                        if(value!=null && value.equals("1") || value.equals("0")){

                            statusBlock2 = Integer.parseInt(value);

                        }
                        else {

                            Log.d("OnGetStatus", "Invalid Status Number Retrieved");

                        }


                    }

                    catch(Exception e){

                        Log.e("StatusRetrieve", e.getMessage(), e);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        catch(Exception e){

            Log.e("onGetStatus", e.getMessage(), e);

        }
    }

    public void notifs(){

        IntentFilter ifl = new IntentFilter();
        ifl.addAction("ok");

        Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(""));
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pit = PendingIntent.getActivity(getApplicationContext(), 0, it, 0);
        Context con = getApplicationContext();

        final NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        CharSequence name = "Imperium";
        String desc = "Monitoring";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        final String cid = "Imperium1";

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(cid, name, importance);
            channel.setDescription(desc);
            notificationManager.createNotificationChannel(channel);

        }

        final int ncode = 1;

        Notification.Builder build;

            build = new Notification.Builder (con)
                    .setContentTitle("ImperiumMonitoring")
                    .setContentText("You are being monitored")
                    .setContentIntent(pit)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setOngoing(true);

        Notification notifs = build.build();


        notificationManager.notify(ncode, notifs);
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
                final PInfo newInfo = new PInfo();
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
                System.out.println("Parent User: "+useremail+" and Child: "+splits[0]);
                rootRef.child(useremail).child("Children").child(splits[0]).child("Apps").child(datum).setValue(true);
            }

            return res;
    }



    public void getCurrentParentUser(){

        try{

            DatabaseReference getuser = FirebaseDatabase.getInstance().getReference();
            getuser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if( dataSnapshot != null){

                        try{

                            String useremailz = dataSnapshot.child("CurrentParent").child(splitss[0]).getValue(String.class);
                            if(useremailz!=null){

                                useremail = useremailz;
                                System.out.println(useremail);
                            }
                            else{

                                Log.d("GetParentUser", "No Parent User Retrieved");
                            }

                        }

                        catch(Exception e){

                            Log.e("ParentUser", e.getMessage(), e);

                        }

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {



                }
            });

        }

        catch(Exception e){

            Log.e("GetParentUser", e.getMessage(), e);

        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

