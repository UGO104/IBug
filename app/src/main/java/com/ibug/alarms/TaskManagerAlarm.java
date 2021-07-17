package com.ibug.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.ibug.misc.Utils.Tag;

public class TaskManagerAlarm {

    public static void stop(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TaskManagerAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, AlarmId.TASK_MANAGER.ID, intent, 0 );

        manager.cancel(pendingIntent);
    }

    public static void start(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TaskManagerAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, AlarmId.TASK_MANAGER.ID, intent, 0
        );

        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, 3 * 60 * 1000, 10 * 60 * 1000, pendingIntent);
    }

    public static class TaskManagerAlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO
            // do period jobs here
            Log.d(Tag, "Alarming...");
//        String callDirectory = getLocalSmsDirectory(getApplicationContext()); // upload to dropbox
//        Dropbox dropbox = new Dropbox(getApplicationContext());
            //dropbox.upload(new File(callDirectory));
        }

    }


}
