package com.app.jamis;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String CHANNEL_ID = "CareerCampus_Channel";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if the message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            // Handle notification payload here
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Notification Title: " + title);
            Log.d(TAG, "Notification Body: " + body);

            // You can extract the data if you want to use custom data payload
            if (remoteMessage.getData().size() > 0) {
                String status = remoteMessage.getData().get("status");
                String employeeName = remoteMessage.getData().get("employeeName");
                String jobCategory = remoteMessage.getData().get("jobCategory");
                long timestamp = Long.parseLong(remoteMessage.getData().get("timestamp"));

                // You can now trigger the FCM notification handler (if needed) or show the notification
                showNotification( status,employeeName, jobCategory, timestamp);
            }
        }
    }

    @Override
    public void onNewToken(String token) {
        // Handle the new token for FCM registration (You can save it on your server)
        Log.d(TAG, "New FCM token: " + token);
        // Here, you would typically send this token to your backend to store it for push notifications
    }

    private void showNotification(String status,String employeeName, String jobCategory, long timestamp) {
        // Create a notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CareerCampus Notifications";
            String description = "Notifications for job applications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        String notificationTitle = "Job Application Update";
        String notificationMessage;

        if ("accepted".equals(status)) {
            notificationMessage = "Congratulations " + employeeName + "! You have been accepted for the " + jobCategory + " position!";
        } else if ("rejected".equals(status)) {
            notificationMessage = "Sorry " + employeeName + ", you have been rejected for the " + jobCategory + " position.";
        } else {
            notificationMessage = employeeName + " applied for the " + jobCategory + " position!";
        }


        // Create a notification to display
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)  // Set your notification icon here
                .setContentTitle("New Job Application")
                .setContentText("You have a new application for the " + jobCategory + " position!")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationMessage))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Show the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) timestamp, notificationBuilder.build());  // Use timestamp to ensure unique notification ID
    }
}
