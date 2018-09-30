package uic.com.imperiumchild;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class MainClass extends Activity {


    Intent mServiceIntent;
    private CheckerService mSensorService;
    Context ctx;
    private static final String TAG = "SignInActivity";

    public Context getCtx() {
        return ctx;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        mSensorService = new CheckerService(getCtx());
        mServiceIntent = new Intent(getCtx(), mSensorService.getClass());

        if (!isMyServiceRunning(mSensorService.getClass())) {

            startService(mServiceIntent);
        }

        finish();
    }

    @Override
    public void onStart(){
        super.onStart();
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    protected void onDestroy() {
        stopService(mServiceIntent);
        super.onDestroy();

    }}