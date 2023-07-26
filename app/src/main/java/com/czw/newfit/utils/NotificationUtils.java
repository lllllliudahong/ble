package com.czw.newfit.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.czw.newfit.R;


public class NotificationUtils {

    private static NotificationUtils utils;
    NotificationManager notificationManager;

    public static NotificationUtils getInstance() {
        if (null == utils) {
            utils = new NotificationUtils();
        }
        return utils;
    }


    @SuppressLint("NotificationPermission")
    public void startNotification(Context context, Class mClass) {

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent1 = new Intent(context, mClass);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent1,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


//        PendingIntent pendingIntent;
//        if (android.os.Build.VERSION.SDK_INT >= 31) {
//            pendingIntent = PendingIntent.getActivity(context, 123, intent1, PendingIntent.FLAG_IMMUTABLE);
//        } else {
//            pendingIntent = PendingIntent.getActivity(context, 123, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
//        }
//


        String channelId = createNotificationChannel("my_channel_ID", "my_channel_NAME", NotificationManager.IMPORTANCE_HIGH, context);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(context.getResources().getString(R.string.app_name)) //标题
                .setContentText("设备使用中") //内容
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(false)
                .setOngoing(false)
//                .setNumber(1)
                .setPriority(Notification.PRIORITY_HIGH)
                .setWhen(System.currentTimeMillis());

        Notification build = notification.build();
        build.flags = Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(16957, build);


    }


    public void cancelNotification() {
        if (notificationManager != null) {
            notificationManager.cancel(16957);

        }
    }


    private String createNotificationChannel(String channelID, String channelNAME, int level, Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(channelID, channelNAME, level);
            manager.createNotificationChannel(channel);
            return channelID;
        } else {
            return null;
        }
    }
}
