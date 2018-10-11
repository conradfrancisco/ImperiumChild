package uic.com.imperiumchild;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StatusBroadcaster extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        context.startService(new Intent(context, CheckerService.class));
        start_lockscreen(context);

    }

    private void start_lockscreen(Context context) {
        Intent mIntent = new Intent(context, StatusLockScreen.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mIntent);
    }

}

