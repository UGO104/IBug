package com.ibug.call;

import java.io.File;
import java.util.Date;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import static com.ibug.misc.Utils.getRootStorage;
import static com.ibug.misc.Utils.recType;

public class IncomingCall extends Intent {

    IncomingCall(Context context) {
        Date date = new Date();
        // set Foreground Services class
        setClass(context, RecordManager.class);
        // pass filename to intent
        putExtra ("duration", date.getTime());
        putExtra ("outputLocation", outputLocation(date));
        // start foreground service
        ContextCompat.startForegroundService(context,this);
    }

    private String outputLocation(Date date) {
        String folder = String.valueOf(date.getTime());
        String filename = "iRec" + folder + recType;
        File canonical = new File (getRootStorage(), folder);
        if (canonical.mkdir()) {
            File fileLocation = new File (canonical, filename);
            return fileLocation.getAbsolutePath();
        }

        return null;
    }

}
