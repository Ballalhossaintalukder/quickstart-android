package com.google.samples.quickstart.functions.java;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.samples.quickstart.functions.R;

public class FunctionsMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MessagingService";

    public FunctionsMessagingService() {
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Messages", "Messages", importance);
            channel.setDescription("All messages.");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        createNotificationChannel();

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            // Check if permission to post notifications has been granted
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat manager = NotificationManagerCompat.from(this);
                Notification notification = new NotificationCompat.Builder(this, "Messages")
                        .setContentText(remoteMessage.getData().get("text"))
                        .setContentTitle("New message")
                        .setSmallIcon(R.drawable.ic_stat_notification)
                        .build();
                manager.notify(0, notification);
            }
        }
    }
}
