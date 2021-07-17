package com.ibug.sms;

import com.ibug.IAnnotations.*;

public class SmsType {

    // TODO ~ content://sms/ reference types
    private  final int MESSAGE_TYPE_ALL    = 0;
    private  final int MESSAGE_TYPE_INBOX  = 1;
    private  final int MESSAGE_TYPE_SENT   = 2;
    private  final int MESSAGE_TYPE_DRAFT  = 3;
    private  final int MESSAGE_TYPE_OUTBOX = 4;
    private  final int MESSAGE_TYPE_FAILED = 5; // for failed outgoing messages
    private  final int MESSAGE_TYPE_QUEUED = 6; // for messages to send later

    @StartMethod
    protected String get(int type) {
        switch (type) {
            case MESSAGE_TYPE_SENT: return "sent";
            case MESSAGE_TYPE_DRAFT: return "draft";
            case MESSAGE_TYPE_INBOX: return "inbox";
            case MESSAGE_TYPE_OUTBOX: return "outbox";
            case MESSAGE_TYPE_FAILED: return "failed";
            case MESSAGE_TYPE_QUEUED: return "queued";
            default: return null;
        }

    }

}
