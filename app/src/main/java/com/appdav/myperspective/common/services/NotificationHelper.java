package com.appdav.myperspective.common.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationManagerCompat;

import com.appdav.myperspective.common.Constants;

public class NotificationHelper {

    private Context context;
    private String title;
    private String text;
    private int smallIcon;

    private Notification.InboxStyle inboxStyle;


    public NotificationHelper setText(String text) {
        this.text = text;
        return this;
    }


    NotificationHelper(Context context) {
        this.context = context;
    }

    NotificationHelper setSmallIcon(@DrawableRes int smallIcon) {
        this.smallIcon = smallIcon;
        return this;
    }

    NotificationHelper setTitle(String title) {
        this.title = title;
        return this;
    }

    NotificationHelper setInboxStyle(String bigContentTitle) {
        inboxStyle = new Notification.InboxStyle();
        inboxStyle.setBigContentTitle(bigContentTitle);
        return this;
    }


    void sendNotification(String notificationChannelName, int notificationId) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(smallIcon);
        if (inboxStyle != null) {
            inboxStyle.addLine(text);
            builder.setStyle(inboxStyle);
        }
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID,
                    notificationChannelName, NotificationManager.IMPORTANCE_HIGH);
            builder.setChannelId(Constants.NOTIFICATION_CHANNEL_ID);
            notificationManagerCompat.createNotificationChannel(channel);
        }
        notificationManagerCompat.notify(notificationId, builder.setStyle(new Notification.BigTextStyle()).build());
    }

}
