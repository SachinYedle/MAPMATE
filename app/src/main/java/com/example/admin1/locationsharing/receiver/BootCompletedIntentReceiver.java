package com.example.admin1.locationsharing.receiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.admin1.locationsharing.services.BackgroundLocationService;

/**
 * Created by admin1 on 20/12/16.
 */



public class BootCompletedIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent pushIntent = new Intent(context, BackgroundLocationService.class);
            context.startService(pushIntent);
        }
    }
}
