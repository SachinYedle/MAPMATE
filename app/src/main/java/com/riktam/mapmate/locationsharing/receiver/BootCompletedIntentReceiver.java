package com.riktam.mapmate.locationsharing.receiver;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.riktam.mapmate.locationsharing.services.BackgroundLocationService;
import com.riktam.mapmate.locationsharing.utils.CustomLog;

/**
 * Created by admin1 on 20/12/16.
 */



public class BootCompletedIntentReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent pushIntent = new Intent(context, BackgroundLocationService.class);
            context.startService(pushIntent);
            scheduleAlarm(context);
        }else {
            CustomLog.d("isServiceRunning"," "+isMyServiceRunning(BackgroundLocationService.class,context));
            if(!isMyServiceRunning(BackgroundLocationService.class,context)){
                Intent pushIntent = new Intent(context, BackgroundLocationService.class);
                context.startService(pushIntent);
            }
        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass,Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void scheduleAlarm(Context context) {
        Intent intent = new Intent(context, BootCompletedIntentReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, BootCompletedIntentReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_HALF_HOUR, pIntent);
    }
}
