package com.ibug.call;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import static com.ibug.misc.Utils.Tag;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class WhatsApp extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            AccessibilityNodeInfo nodeInfo = event.getSource();
            if (nodeInfo == null ) return;
            performNodeTask(nodeInfo);
            nodeInfo.recycle();
        }
        catch (Exception e){}
//        if (event.getEventType() ==  AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){
//            AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
//            if (nodeInfo != null) {
//                Log.d(Tag, "parent count " + nodeInfo.getChildCount());
//                for (int count = 0; count<nodeInfo.getChildCount(); count++) {
//                    AccessibilityNodeInfo child = nodeInfo.getChild(count);
//                    String className = (String) child.getClassName();
////                    Log.d(Tag, className);
//                    if (className.endsWith("FrameLayout")) {
//                        Log.d(Tag, "child count " + child.getChildCount());
//                        for (int count0 = 0; count0<nodeInfo.getChildCount(); count0++) {
//                            String className0 = (String) child.getClassName();
//                            Log.d(Tag, className0);
//                        }
////                        Log.d(Tag, String.valueOf(child.getContentDescription()));
//                    }
////                    Log.d(Tag, (String)child.getContentDescription());
//                }
//            }
//        }
////                        if (!event.getText().isEmpty()) {
////                    for (CharSequence seq: event.getText()) {
////                        Log.d(Tag, seq.toString());
////                    }
////                }
//
//////        switch (event.getEventType()) {
//////            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED: {
//////                if (!event.getText().isEmpty()) {
//////                    for (CharSequence seq: event.getText()) {
//////                        Log.d(Tag, seq.toString());
//////                    }
//////                }
//////                break;
//////            }
//////
//////            case AccessibilityEvent.TYPE_VIEW_CLICKED: {
//////                Log.d(Tag, "TYPE_VIEW_CLICKED");
////////                String eventText = event.getText();
//////                if (!event.getText().isEmpty()) {
//////                    for (CharSequence seq: event.getText()) {
//////                        Log.d(Tag, seq.toString());
//////                    }
//////                }
//////                break;
//////            }
//////
////        }

    }

    private void performNodeTask(AccessibilityNodeInfo rootNode) {
        AccessibilityNodeInfo view = getNodeInfo(rootNode, "com.whatsapp:id/send");
        if (view == null) return;
    }

    private AccessibilityNodeInfo getNodeInfo(AccessibilityNodeInfo rootNode, String viewId) {
        AccessibilityNodeInfo node = null;
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId);
        if (nodes.size() > 0) node = nodes.get(0);
         return node;
    }

    @Override
    public void onInterrupt() {

    }

    private final String VOICE_CALL = "Voice call";
    private final String VIDEO_CALL = "Video call";
    private final String END_VOICE_CALL = "End Voice Call";
    private final String END_VIDEO_CALL = "End Video Call";

}
