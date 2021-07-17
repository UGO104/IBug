package com.ibug.alarms;

public enum AlarmId {
    SENT_SMS(100001),
    TASK_MANAGER(100002);
    public final int ID;
    private AlarmId(int _id) {
        ID = _id;
    }

}
