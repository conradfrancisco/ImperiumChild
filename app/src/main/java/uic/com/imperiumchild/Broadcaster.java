package uic.com.imperiumchild;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Broadcaster extends BroadcastReceiver {

    private String value = "";

    @Override
    public void onReceive(Context context, Intent intent) {

        try{

            Log.d("STARTED", "Receive Intent");
            try {
                InputStream inputStream = context.openFileInput("test.txt");

                if ( inputStream != null ) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ( (receiveString = bufferedReader.readLine()) != null ) {
                        stringBuilder.append(receiveString);
                        Log.d("OnReceive", "Reading from Text File");
                    }

                    inputStream.close();
                    value = stringBuilder.toString();
                    System.out.println(value);
                }
            }
            catch (FileNotFoundException e) {

                Log.e("login activity", "File not found: " + e.toString());
            }

            catch (IOException e) {

                Log.e("login activity", "Can not read file: " + e.toString());
            }

            if(value != null){

                if(value.equals("1")){

                    Log.d("OnReceive", "Startling LOCK SCREEN");
                    start_lockscreen(context);

                }

            }

            Log.d("OnReceive", "Starting Service");
            context.startService(new Intent(context, CheckerService.class));

            if(Intent.ACTION_TIME_TICK.equals(intent.getAction())){

                Log.d("OnReceive with Intent", "Startling LOCK SCREEN");
                start_lockscreen(context);

            }
        }

        catch(Exception e){

            Log.e("onReceiveLockScreen", e.getMessage(), e);

        }

    }

    private void start_lockscreen(Context context) {
        Intent mIntent = new Intent(context, LockScreenActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mIntent);
    }

}
