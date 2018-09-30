package uic.com.imperiumchild;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Broadcaster extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){

            context.startService(new Intent(context, CheckerService.class));

        }
        else {

            context.startService(new Intent(context, CheckerService.class));

        }


    }

}
