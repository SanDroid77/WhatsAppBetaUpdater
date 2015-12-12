package com.sandroid.updaterforwhatsappbeta;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by sandro on 31/05/2015.
 */
public class MyReceiver extends BroadcastReceiver {
    private int notificationID = 1;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getExtras().getBoolean("isAvailableVersion")) {
            String title = context.getResources().getString(R.string.notification_title);
            String message = context.getResources().getString(R.string.notification_text);

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            Intent mainIntent = new Intent(context, MainActivity.class);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.notification_icon_dl)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setPriority(-1) // PRIORITY_LOW
                    .setVisibility(1) // VISIBILITY_PUBLIC
                    .setVibrate(new long[] {100,200,100,250});

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(mainIntent);

            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_CANCEL_CURRENT
                    );

            mBuilder.setContentIntent(resultPendingIntent);
            mNotificationManager.notify(notificationID, mBuilder.build());
            Log.d("Version", "New version");
        } else {
            Log.d("Version", "No new version");
        }
    }
}
