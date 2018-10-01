package uic.com.imperiumchild;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainClass extends Activity {


    private FirebaseAuth auth;
    Intent mServiceIntent;
    private CheckerService mSensorService;
    Context ctx;
    private static final String TAG = "SignInActivity";
    String passval = "";
    private String user = "";
    String passvalue = "";
    FirebaseUser current;

    public Context getCtx() {
        return ctx;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        current = auth.getCurrentUser();
        passval = current.getEmail();
        System.out.println(passval);
        getCurrentUser();

        ctx = this;
        mSensorService = new CheckerService(getCtx());
        mServiceIntent = new Intent(getCtx(), mSensorService.getClass());

        if (!isMyServiceRunning(mSensorService.getClass())) {

            mServiceIntent.putExtra("Username", passvalue);
            startService(mServiceIntent);
        }

        finish();
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
                    passvalue = user;

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {



            }
        });

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