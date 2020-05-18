package in.omdev.notificationhistory.component;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import androidx.room.Room;
import in.omdev.notificationhistory.R;
import in.omdev.notificationhistory.database.NotificationDatabase;
import in.omdev.notificationhistory.database.NotificationEntity;
import in.omdev.notificationhistory.ui.MainActivity;

import static in.omdev.notificationhistory.Const.NOTIFICATION_ID;
import static in.omdev.notificationhistory.Const.ONGOING_CHANNEL_ID;

public class ListenerService extends NotificationListenerService {

    public static boolean insertOngoing = false;
    public static int maxEntries = 50000;

    private NotificationDatabase database;

    private int entries = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, ONGOING_CHANNEL_ID)
                .setContentTitle(getString(R.string._notification_title))
                .setContentText(getString(R.string._notification_message))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string._ticker_text));
        startForeground(NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        database = Room.databaseBuilder(getApplicationContext(),
                NotificationDatabase.class,
                "notification_history").build();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        insertOngoing = preferences
                .getBoolean("insertOngoing", false);
        maxEntries = Integer.parseInt(preferences.getString("maxEntries", "50000"));
        AsyncTask.execute(() -> entries = database.notificationDao().getRowCount());
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        AsyncTask.execute(() -> {
            if (sbn.isOngoing() && !insertOngoing)
                return;
            NotificationEntity notification = new NotificationEntity();
            notification.notificationId = sbn.getId();
            notification.key = sbn.getKey();
            notification.packageName = sbn.getPackageName();
            notification.tag = sbn.getTag();
            notification.systemTime = System.currentTimeMillis();
            notification.when = sbn.getNotification().when;
            notification.color = sbn.getNotification().color;
            if(sbn.getNotification().tickerText != null)
                notification.tickerText = sbn.getNotification().tickerText.toString();
            notification.visibility = sbn.getNotification().visibility;
            notification.title = sbn
                    .getNotification().extras.getString(Notification.EXTRA_TITLE);
            notification.text = sbn
                    .getNotification().extras.getString(Notification.EXTRA_TEXT);
            notification.bigText = sbn
                    .getNotification().extras.getString(Notification.EXTRA_BIG_TEXT);
            CharSequence[] charSequences = sbn
                    .getNotification().extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
            notification.textLines = "";
            if (charSequences != null) {
                notification.textLines = TextUtils.join("\n", charSequences);
            }
            database.notificationDao().insertAll(notification);
            getSharedPreferences("notifier", MODE_PRIVATE).edit()
                    .putLong("last", System.currentTimeMillis()).apply();
            if (++entries > maxEntries) {
                database.notificationDao().deleteLast(entries-maxEntries);
                entries = maxEntries;
            }
        });
    }
}
