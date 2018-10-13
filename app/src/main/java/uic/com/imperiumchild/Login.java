package uic.com.imperiumchild;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Conrad Francisco Jr on 6/12/2018.
 */

public class Login extends AppCompatActivity {

    private FirebaseAuth auth;
    boolean isConnected = true;
    private String value = "";
    Context context = this;
    private boolean monitoringConnectivity = false;
    private EditText inputuser;
    private ConstraintLayout constraint;
    private Button login;
    private ProgressBar bar;
    String parent = "";
    String user = "";
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    final String password = "imperium123";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    auth = FirebaseAuth.getInstance();
    constraint = (ConstraintLayout) findViewById(R.id.coordinatorlogin);

        if(auth.getCurrentUser() != null){

            startActivity(new Intent(Login.this, MainClass.class));
            finish();
        }

    inputuser = (EditText) findViewById(R.id.user);
    login = (Button) findViewById(R.id.login);
    bar = (ProgressBar) findViewById(R.id.progressBar);
    Intent intent = new Intent(Login.this, CheckerService.class);
    stopService(intent);
    FirebaseApp.initializeApp(Login.this);
    firebaseDatabase = FirebaseDatabase.getInstance();
    databaseReference = firebaseDatabase.getReference();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try{

                    user = inputuser.getText().toString();
                    bar.setVisibility(View.VISIBLE);

                    if(TextUtils.isEmpty(user)) {

                        Snackbar sn = Snackbar.make(constraint, "A Username is required!", Snackbar.LENGTH_SHORT);
                        sn.show();
                        return;
                    }
                    if(!isConnected){

                        Snackbar sn = Snackbar.make(constraint, R.string.auth_failed, Snackbar.LENGTH_SHORT);
                        sn.show();
                        inputuser.setText("");
                    }

                    else if (!Patterns.EMAIL_ADDRESS.matcher(user).matches()){
                        Toast.makeText(getApplicationContext(), "Enter a Valid Email Address!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else {

                        try{

                            final String ape[]  = user.split("@");
                            DatabaseReference getuser = FirebaseDatabase.getInstance().getReference("CurrentParent").child(ape[0]);
                            getuser.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    parent = dataSnapshot.getValue(String.class);
                                    if(parent!=null){

                                        DatabaseReference getusers = FirebaseDatabase.getInstance().getReference("Users").child(parent);
                                        getusers.child("Children").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                for(DataSnapshot name : dataSnapshot.getChildren()){

                                                    if(name!=null){

                                                        String nem = name.getKey();
                                                        logins(nem);


                                                    }

                                                    else {

                                                        Log.d("Login Child", "No Children Found");

                                                    }

                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                    else{

                                        Log.d("Login Child", "No Parent Found");
                                        Snackbar sn = Snackbar.make(constraint, "This Account has not been added by a Parent Device!", Snackbar.LENGTH_LONG);
                                        sn.show();
                                        inputuser.setText(null);
                                        bar.setVisibility(View.GONE);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }

                        catch(Exception e){

                            Log.e("to Sign In", e.getMessage(), e);

                        }
                    }

                }

                catch(Exception e){

                    Log.e("Login Child", e.getMessage(), e);

                }

            }
        });

    }

    public void logins(final String usernem){

        final String ape[]  = user.split("@");
        if(usernem.equals(ape[0])){

            System.out.println("Current Username: "+usernem+"Current User: "+ape[0]);
            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
            builder.setTitle("Please confirm action!");
            builder.setMessage("Are all the information provided True and Correct?");
            builder.setIcon(R.drawable.icon);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    auth.createUserWithEmailAndPassword(user, password)
                            .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    bar.setVisibility(View.GONE);

                                    if(!task.isSuccessful()){

                                        Log.w("Registration", "signInWithCredential", task.getException());

                                        if(task.getException() instanceof FirebaseAuthUserCollisionException) {

                                            final Snackbar sn = Snackbar.make(constraint, "Account already Registered, Signing In!", Snackbar.LENGTH_INDEFINITE);
                                            sn.show();
                                            inputuser.setText(null);

                                            final String newpass = password;
                                            final String newuser = user;

                                            try {

                                                auth.signInWithEmailAndPassword(newuser, newpass)
                                                        .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                                bar.setVisibility(View.GONE);
                                                                sn.dismiss();
                                                                if(task.isSuccessful()){

                                                                    Snackbar sn = Snackbar.make(constraint, "Login Successfully!", Snackbar.LENGTH_SHORT);
                                                                    sn.show();
                                                                    startActivity(new Intent(Login.this, MainClass.class));
                                                                    finish();

                                                                }
                                                                else {

                                                                    Snackbar sn = Snackbar.make(constraint, R.string.auth_failed, Snackbar.LENGTH_SHORT);
                                                                    sn.show();
                                                                    inputuser.setText("");

                                                                }

                                                            }

                                                        });

                                            }

                                            catch (Exception e) {

                                                Log.e("SignIn", e.getMessage(), e);
                                            }

                                        }

                                        else if(task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                                            Toast.makeText(Login.this, "Please check your EMail Address!", Toast.LENGTH_LONG).show();
                                            inputuser.setText(null);

                                        }

                                    }
                                    else if(task.isSuccessful()) {

                                        Snackbar sn = Snackbar.make(constraint, "Login Successfully!", Snackbar.LENGTH_SHORT);
                                        sn.show();
                                        startActivity(new Intent(Login.this, MainClass.class));
                                        finish();


                                    }
                                }
                            });
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    dialogInterface.dismiss();
                    Toast.makeText(getApplicationContext(), "Log-In Cancelled!", Toast.LENGTH_SHORT).show();
                    inputuser.setText(null);
                    bar.setVisibility(View.GONE);

                }
            });
            android.support.v7.app.AlertDialog alert = builder.create();
            alert.show();

        }
//
//        else if(!usernem.equals(ape[0])) {
//
//            Snackbar sn = Snackbar.make(constraint, "2 This Account has not been added by a Parent Device!", Snackbar.LENGTH_LONG);
//            sn.show();
//            inputuser.setText(null);
//
//        }

        else {

            Log.d("Sign In", "Sign-in Failed");

        }

    }
    @Override
    protected void onResume() {

        super.onResume();
        checkConnectivity();
    }

    @Override
    protected void onPause() {

        if (monitoringConnectivity) {

            final ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.unregisterNetworkCallback(connectivityCallback);
            monitoringConnectivity = false;

        }

        super.onPause();

    }

    public void onBackPressed(){

        super.onBackPressed();
        finish();

    }

    private ConnectivityManager.NetworkCallback connectivityCallback
            = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            isConnected = true;

            Snackbar sn = Snackbar.make(constraint, "Connected!", Snackbar.LENGTH_SHORT);
            sn.show();

        }
        @Override
        public void onLost(Network network) {
            isConnected = false;

            Snackbar sn = Snackbar.make(constraint, "No Connection!", Snackbar.LENGTH_INDEFINITE);
            sn.show();
        }
    };

    private void checkConnectivity() {

        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);

        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if (!isConnected) {

            Snackbar sn = Snackbar.make(constraint, "No Connection!", Snackbar.LENGTH_INDEFINITE);
            sn.show();
            connectivityManager.registerNetworkCallback(
                    new NetworkRequest.Builder()
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .build(), connectivityCallback);
            monitoringConnectivity = true;
        }
        else {

            connectivityManager.registerNetworkCallback(
                    new NetworkRequest.Builder()
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .build(), connectivityCallback);
            monitoringConnectivity = true;

        }

    }

}

