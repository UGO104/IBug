package com.ibug;

import androidx.core.app.NotificationCompat;

public interface ForegroundService {
    void _startForeground(NotificationCompat.Builder builder);
}
