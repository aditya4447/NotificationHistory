package in.omdev.notificationhistory.ui;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.transition.Transition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import in.omdev.notificationhistory.Const;
import in.omdev.notificationhistory.R;
import in.omdev.notificationhistory.database.NotificationDatabase;
import in.omdev.notificationhistory.database.NotificationEntity;

public class NotificationActivity extends AppCompatActivity {

    private TextView appName;
    private LinearLayout info_container;
    private NotificationEntity notification;
    private NotificationDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Bundle extras = getIntent().getExtras();
        if (extras == null || extras.getInt("id", 0) == 0)
            return;
        TextView title = findViewById(R.id.textView_title);
        TextView text = findViewById(R.id.textView_text);
        appName = findViewById(R.id.textView_app_name);
        ImageView icon = findViewById(R.id.imageView_app_icon);
        info_container = findViewById(R.id.info_container);
        title.setText(extras.getString("title"));
        text.setText(extras.getString("text"));
        PackageManager packageManager = getPackageManager();
        String packageName = extras.getString("packageName");
        if (packageManager != null && packageName != null) {
            try {
                icon.setImageDrawable(packageManager.getApplicationIcon(packageName));
            } catch (PackageManager.NameNotFoundException e) {
                icon.setImageDrawable(packageManager.getDefaultActivityIcon());
            }
            try {
                appName.setText(packageManager.getApplicationLabel(packageManager
                        .getApplicationInfo(packageName, 0)));
            } catch (PackageManager.NameNotFoundException e) {
                appName.setText(extras.getString("packageName"));
            }
        }
        getWindow().getSharedElementEnterTransition().addListener(
                new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                if (appName.getVisibility() == View.VISIBLE) {
                    appName.setVisibility(View.GONE);
                    info_container.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                if (appName.getVisibility() == View.GONE) {
                    appName.setVisibility(View.VISIBLE);
                    info_container.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });
        getWindow().getSharedElementExitTransition().addListener(
                new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                appName.setVisibility(View.GONE);
                info_container.setVisibility(View.GONE);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });
        database = Room.databaseBuilder(
                getApplicationContext(),
                NotificationDatabase.class, "notification_history").build();
        AsyncTask.execute(() -> {
            notification = database.notificationDao()
                    .get(extras.getInt("id"));

            //title
            @SuppressLint("InflateParams") View titleInfo = LayoutInflater
                    .from(NotificationActivity.this).inflate(R.layout.item_info, null);
            ((TextView) titleInfo.findViewById(R.id.info_title))
                    .setText(R.string.title);
            ((TextView) titleInfo.findViewById(R.id.info_text))
                    .setText(notification.title);
            runOnUiThread(() -> info_container.addView(titleInfo));

            //text
            @SuppressLint("InflateParams") View textInfo = LayoutInflater
                    .from(NotificationActivity.this).inflate(R.layout.item_info, null);
            ((TextView) textInfo.findViewById(R.id.info_title))
                    .setText(R.string.text);
            ((TextView) textInfo.findViewById(R.id.info_text))
                    .setText(notification.text);
            runOnUiThread(() -> info_container.addView(textInfo));

            //big text
            @SuppressLint("InflateParams") View bigTextInfo = LayoutInflater
                    .from(NotificationActivity.this).inflate(R.layout.item_info, null);
            ((TextView) bigTextInfo.findViewById(R.id.info_title))
                    .setText(R.string.big_text);
            ((TextView) bigTextInfo.findViewById(R.id.info_text))
                    .setText(notification.bigText);
            runOnUiThread(() -> info_container.addView(bigTextInfo));

            //text lines
            @SuppressLint("InflateParams") View textLinesInfo = LayoutInflater
                    .from(NotificationActivity.this).inflate(R.layout.item_info, null);
            ((TextView) textLinesInfo.findViewById(R.id.info_title))
                    .setText(R.string.text_lines);
            ((TextView) textLinesInfo.findViewById(R.id.info_text))
                    .setText(notification.textLines);
            runOnUiThread(() -> info_container.addView(textLinesInfo));

            //ticker text
            @SuppressLint("InflateParams") View tickerTextInfo = LayoutInflater
                    .from(NotificationActivity.this).inflate(R.layout.item_info, null);
            ((TextView) tickerTextInfo.findViewById(R.id.info_title))
                    .setText(R.string.ticker_text);
            ((TextView) tickerTextInfo.findViewById(R.id.info_text))
                    .setText(notification.tickerText);
            runOnUiThread(() -> info_container.addView(tickerTextInfo));

            //id
            @SuppressLint("InflateParams") View idInfo = LayoutInflater
                    .from(NotificationActivity.this).inflate(R.layout.item_info, null);
            ((TextView) idInfo.findViewById(R.id.info_title))
                    .setText(R.string.id);
            ((TextView) idInfo.findViewById(R.id.info_text))
                    .setText(String.valueOf(notification.notificationId));
            runOnUiThread(() -> info_container.addView(idInfo));

            //key
            @SuppressLint("InflateParams") View keyInfo = LayoutInflater
                    .from(NotificationActivity.this).inflate(R.layout.item_info, null);
            ((TextView) keyInfo.findViewById(R.id.info_title))
                    .setText(R.string.key);
            ((TextView) keyInfo.findViewById(R.id.info_text))
                    .setText(notification.key);
            runOnUiThread(() -> info_container.addView(keyInfo));

            //package
            @SuppressLint("InflateParams") View packageInfo = LayoutInflater
                    .from(NotificationActivity.this).inflate(R.layout.item_info, null);
            ((TextView) packageInfo.findViewById(R.id.info_title))
                    .setText(R.string.package_name);
            ((TextView) packageInfo.findViewById(R.id.info_text))
                    .setText(notification.packageName);
            runOnUiThread(() -> info_container.addView(packageInfo));

            //system time
            @SuppressLint("InflateParams") View systemTimeInfo = LayoutInflater
                    .from(NotificationActivity.this).inflate(R.layout.item_info, null);
            ((TextView) systemTimeInfo.findViewById(R.id.info_title))
                    .setText(R.string.system_time);
            ((TextView) systemTimeInfo.findViewById(R.id.info_text))
                    .setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                            .format(new Date(notification.systemTime)));
            runOnUiThread(() -> info_container.addView(systemTimeInfo));

            //post time
            @SuppressLint("InflateParams") View postTimeInfo = LayoutInflater
                    .from(NotificationActivity.this).inflate(R.layout.item_info, null);
            ((TextView) postTimeInfo.findViewById(R.id.info_title))
                    .setText(R.string.post_time);
            if (notification.postTime != 0) {
                ((TextView) postTimeInfo.findViewById(R.id.info_text))
                        .setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                                .format(new Date(notification.postTime)));
            }
            runOnUiThread(() -> info_container.addView(postTimeInfo));


            //when
            @SuppressLint("InflateParams") View when = LayoutInflater
                    .from(NotificationActivity.this).inflate(R.layout.item_info, null);
            ((TextView) when.findViewById(R.id.info_title))
                    .setText(R.string.when);
            if (notification.when != 0) {
                ((TextView) when.findViewById(R.id.info_text))
                        .setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                                .format(new Date(notification.when)));
            }
            runOnUiThread(() -> info_container.addView(when));

            //tag
            @SuppressLint("InflateParams") View tagInfo = LayoutInflater
                    .from(NotificationActivity.this).inflate(R.layout.item_info, null);
            ((TextView) tagInfo.findViewById(R.id.info_title))
                    .setText(R.string.tag);
            ((TextView) tagInfo.findViewById(R.id.info_text))
                    .setText(notification.tag);
            runOnUiThread(() -> info_container.addView(tagInfo));

            //is ongoing
            @SuppressLint("InflateParams") View ongoingInfo = LayoutInflater
                    .from(NotificationActivity.this).inflate(R.layout.item_info, null);
            ((TextView) ongoingInfo.findViewById(R.id.info_title))
                    .setText(R.string.ongoing);
            ((TextView) ongoingInfo.findViewById(R.id.info_text))
                    .setText(String.valueOf(notification.isOngoing));
            runOnUiThread(() -> info_container.addView(ongoingInfo));

            //key
            @SuppressLint("InflateParams") View colorInfo = LayoutInflater
                    .from(NotificationActivity.this).inflate(R.layout.item_info, null);
            ((TextView) colorInfo.findViewById(R.id.info_title))
                    .setText(R.string.color);
            ((TextView) colorInfo.findViewById(R.id.info_text))
                    .setText(String.valueOf(notification.color));
            runOnUiThread(() -> info_container.addView(colorInfo));

            //key
            @SuppressLint("InflateParams") View visibilityInfo = LayoutInflater
                    .from(NotificationActivity.this).inflate(R.layout.item_info, null);
            ((TextView) visibilityInfo.findViewById(R.id.info_title))
                    .setText(R.string.visibility);
            ((TextView) visibilityInfo.findViewById(R.id.info_text))
                    .setText(String.valueOf(notification.visibility));
            runOnUiThread(() -> info_container.addView(visibilityInfo));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_notification, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_delete) {
            AsyncTask.execute(() -> {
                if (database != null && notification != null) {
                    database.notificationDao().delete(notification);
                }
            });
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
