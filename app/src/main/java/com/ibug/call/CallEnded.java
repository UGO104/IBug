package com.ibug.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ibug.IAnnotations.*;
import static com.ibug.misc.Utils.Tag;

public abstract class CallEnded extends BroadcastReceiver {

    @AbstractMethod
    public abstract void doWork();

    @Override
    public void onReceive(Context context, Intent intent) {
        doWork();
        Log.d(Tag, "Call ended...");
    }

}
