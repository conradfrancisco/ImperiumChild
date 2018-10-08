package uic.com.imperiumchild;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;

public class LockScreenActivity extends Activity {


    private int isBlocked;
    private ComponentName compName;
    public static final int RESULT_ENABLE = 11;
    private DevicePolicyManager devicePolicyManager;
    FirebaseUser current;
    String value = "";
    String user = "";
    String passval = "";
    String useremail;
    String splitss[];
    private FirebaseAuth auth;
    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;


    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        current = auth.getCurrentUser();
        passval = current.getEmail();
        splitss = passval.split("@");
        compName = new ComponentName(this, AdminClass.class);
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        getCurrentParentUser();
        startTimer();
        Boolean active = devicePolicyManager.isAdminActive(compName);

        if(active){

            makeFullScreen();
            startService(new Intent(this, CheckerService.class));
            setContentView(R.layout.activity_lockscreen);
            HomeKeyLocker homeKeyLocker = new HomeKeyLocker();
            homeKeyLocker.lock(this);
            devicePolicyManager.lockNow();

        }
        else {

            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "You need me :)");
            makeFullScreen();
            startService(new Intent(this, CheckerService.class));
            setContentView(R.layout.activity_lockscreen);
            HomeKeyLocker homeKeyLocker = new HomeKeyLocker();
            homeKeyLocker.lock(this);
            devicePolicyManager.lockNow();
            startActivityForResult(intent, RESULT_ENABLE);

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case RESULT_ENABLE :
                if (resultCode == Activity.RESULT_OK) {

                    Toast.makeText(LockScreenActivity.this, "Admin Device Features enabled!", Toast.LENGTH_SHORT).show();


                } else {

                    Toast.makeText(LockScreenActivity.this, "Problem to enable the Admin Device features", Toast.LENGTH_SHORT).show();

                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {

        super.onResume();

    }

    public void startTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 5000, 5000);
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {

                getDeviceBlock();

            }
        };
    }


    public void makeFullScreen(){

        this.getWindow().setType(
                WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        this.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        );
        this.getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);

    }

    public void getDeviceBlock(){

        DatabaseReference getdev = FirebaseDatabase.getInstance().getReference();
        getdev.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                System.out.println("Current Parent User: "+useremail+" and Current Child User: "+splitss[0]);
                String value = dataSnapshot.child("Users").child(useremail).child("Children").child(splitss[0]).child("BlockDevice").getValue(String.class);
                int intval = Integer.parseInt(value);
                System.out.println("Ako karon kay: "+value);
                if(intval == 0){

                    isBlocked = 0;
                    HomeKeyLocker homeKeyLocker = new HomeKeyLocker();
                    homeKeyLocker.unlock();
//                    startActivity(new Intent(LockScreenActivity.this, Login.class));
                    finish();
                }

                else {

                    isBlocked = 1;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void getCurrentParentUser(){

        DatabaseReference getuser = FirebaseDatabase.getInstance().getReference();
        getuser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if( dataSnapshot != null){

                    useremail = dataSnapshot.child("Current").child("currentuser").getValue(String.class);
                    System.out.println(useremail);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {



            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
                || (keyCode == KeyEvent.KEYCODE_POWER)
                || (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                || (keyCode == KeyEvent.KEYCODE_CAMERA)) {
            return true;
        }
        if ((keyCode == KeyEvent.KEYCODE_HOME)) {

            return true;
        }

        return false;

    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
                || (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
                || (event.getKeyCode() == KeyEvent.KEYCODE_POWER)) {
            return false;
        }
        if ((event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {

            return true;
        }
        return false;
    }


    @Override
    public void onBackPressed() {
        return;
    }

}
