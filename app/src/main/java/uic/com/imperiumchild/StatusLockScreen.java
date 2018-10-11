package uic.com.imperiumchild;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
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

public class StatusLockScreen extends Activity {

    private int isBlocked;
    private ConstraintLayout constraint;
    private Button accom;
    private TextView assigned, waiting;
    private FirebaseAuth auth;
    private DatabaseReference ref;
    FirebaseUser current;
    String value = "";
    String user = "";
    String passval = "";
    String useremail = "";
    String usertasks = "";
    String splitss[];
    private ComponentName compName;
    public static final int RESULT_ENABLE = 11;
    private DevicePolicyManager devicePolicyManager;
    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statuslock);
        getCurrentParentUser();
        ref = FirebaseDatabase.getInstance().getReference("Users");
        auth = FirebaseAuth.getInstance();
        current = auth.getCurrentUser();
        constraint = (ConstraintLayout) findViewById(R.id.constraint);
        passval = current.getEmail();
        waiting = (TextView) findViewById(R.id.waiting);
        accom = (Button) findViewById(R.id.accom);
        assigned = (TextView) findViewById(R.id.assigned);
        splitss = passval.split("@");
        compName = new ComponentName(this, AdminClass.class);
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        startTimer();
        final Boolean active = devicePolicyManager.isAdminActive(compName);

        if(active) {

            makeFullScreen();
            startService(new Intent(StatusLockScreen.this, CheckerService.class));
            HomeKeyLocker homeKeyLocker = new HomeKeyLocker();
            homeKeyLocker.lock(StatusLockScreen.this);
            devicePolicyManager.lockNow();
        }

        else if (!active) {

             Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
             intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
             intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "You need me :)");
             startActivityForResult(intent, RESULT_ENABLE);

        }
        accom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ref.child(useremail).child("Children").child(splitss[0]).child("HardStatus").setValue("1");
                accom.setVisibility(View.GONE);
                waiting.setVisibility(View.VISIBLE);

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case RESULT_ENABLE :
                if (resultCode == Activity.RESULT_OK) {

                    makeFullScreen();
                    startService(new Intent(this, CheckerService.class));
                    HomeKeyLocker homeKeyLocker = new HomeKeyLocker();
                    homeKeyLocker.lock(this);
                    devicePolicyManager.lockNow();
                    Toast.makeText(StatusLockScreen.this, "Admin Device Features enabled!", Toast.LENGTH_SHORT).show();


                } else {

                    Toast.makeText(StatusLockScreen.this, "Problem to enable the Admin Device features", Toast.LENGTH_SHORT).show();

                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {

        super.onResume();

    }

    public void getCurrentParentUser(){

        DatabaseReference getuser = FirebaseDatabase.getInstance().getReference();
        getuser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if( dataSnapshot != null){

                    try{

                        String useremailz = dataSnapshot.child("Current").child("currentuser").getValue(String.class);
                        if (useremailz != null) {

                            useremail = useremailz;
                            System.out.println(useremail);

                            DatabaseReference getusers = FirebaseDatabase.getInstance().getReference();
                            getusers.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    String usertasksz = dataSnapshot.child("Users").child(useremail).child("Children").child(splitss[0]).child("Tasks").child("To-Do").getValue(String.class);
                                    if(usertasksz!=null){

                                        usertasks = usertasksz;
                                        System.out.println("Current Task is: "+usertasks);
                                        assigned.setText(usertasks);

                                    }
                                    else{

                                        Toast.makeText(getApplicationContext(), "No Tasks Found", Toast.LENGTH_SHORT).show();

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                        else{

                            Toast.makeText(getApplicationContext(), "No Current Parent Found", Toast.LENGTH_SHORT).show();
                        }

                    }

                    catch(Exception e){

                        Log.e("StatusLock", e.getMessage(), e);

                    }



                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {



            }
        });

    }

    public void startTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 5000, 1000);
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {

                getDeviceBlock();

            }
        };
    }

    public void getDeviceBlock(){

        DatabaseReference getdev = FirebaseDatabase.getInstance().getReference();
        getdev.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                try{

                    System.out.println("Current Parent User: "+useremail+" and Current Child User: "+splitss[0]);
                    String value = dataSnapshot.child("Users").child(useremail).child("Children").child(splitss[0]).child("Tasks").child("Status").getValue(String.class);
                    if(value != null){


                        int intval = Integer.parseInt(value);
                        System.out.println("Ako karon kay: "+value);
                        if(intval == 0){

                            isBlocked = 0;
                            HomeKeyLocker homeKeyLocker = new HomeKeyLocker();
                            homeKeyLocker.unlock();
                            finish();
                        }

                        else {

                            isBlocked = 1;
                        }

                    }
                    else{

                        Toast.makeText(getApplicationContext(), "No Data Recieved!", Toast.LENGTH_SHORT).show();

                    }

                }

                catch(Exception e){

                    Log.e("DeviceLock", e.getMessage(), e);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
