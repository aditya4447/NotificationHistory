package in.omdev.notificationhistory;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;

import in.omdev.notificationhistory.component.ListenerMonitorService;

import static in.omdev.notificationhistory.Const.ONGOING_CHANNEL_ID;

public class NotificationHistory extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string._ongoing_channel_name);
            String description = getString(R.string._channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(ONGOING_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        startService(new Intent(this, ListenerMonitorService.class));
    }
}
