package in.omdev.notificationhistory.ui;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import in.omdev.notificationhistory.R;
import in.omdev.notificationhistory.database.AboutActivity;
import in.omdev.notificationhistory.database.NotificationDao;
import in.omdev.notificationhistory.database.NotificationDatabase;

import static android.graphics.PorterDuff.Mode.SRC_IN;
import static in.omdev.notificationhistory.Const.ONGOING_CHANNEL_ID;

public class MainActivity extends AppCompatActivity {

    private NotificationDatabase database;

    private NotificationAdapter adapter;

    private boolean isEmpty = true;
    private TextView textView_noHistory;
    private SwipeRefreshLayout swipeRefresh_notifications;

    private DatePickerDialog datePickerDialog;

    private boolean appendNotification = true;

    private MenuItem select_date;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init extra views
        textView_noHistory = findViewById(R.id.textView_noHistory);
        swipeRefresh_notifications = findViewById(R.id.swipeRefresh_notifications);

        //init recycler view
        RecyclerView recyclerView_notifications = findViewById(R.id.recyclerView_notifications);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView_notifications.setLayoutManager(layoutManager);
        recyclerView_notifications.addItemDecoration(new DividerItemDecoration(this,
                layoutManager.getOrientation()));


        //database instance
        database = Room.databaseBuilder(
                getApplicationContext(),
                NotificationDatabase.class, "notification_history").build();

        Calendar calendar = Calendar.getInstance();

        //datePickerDialog for filtering
        datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar filterDate = Calendar.getInstance();
                    filterDate.set(
                            year, month, dayOfMonth,
                            0, 0, 0);
                    refreshNotifications(filterDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        AsyncTask.execute(() -> {
            adapter = new NotificationAdapter(database.notificationDao().getAllMini(),
                    new NotificationAdapter.NotificationDiff(), this);
            runOnUiThread(() -> {
                recyclerView_notifications.setAdapter(adapter);
                if (adapter.getItemCount() > 0) {
                    textView_noHistory.setVisibility(View.GONE);
                    isEmpty = false;
                }
            });
        });

        //check if  we have notification access
        if (!NotificationManagerCompat.getEnabledListenerPackages
                (MainActivity.this).contains(getPackageName()) &&
                getSharedPreferences("no_backup", MODE_PRIVATE).getBoolean(
                        "askAccessPermission", true)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.permission)
                    .setMessage(R.string._notification_access_permission_summary)
                    .setPositiveButton(R.string.yes, ((dialog, which) -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            startActivity(new Intent(
                                    Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                        } else {
                            startActivity(new Intent(
                                    "android.settings" +
                                            ".ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                        }
                        dialog.dismiss();
                    }))
                    .setNeutralButton(getString(R.string.never), (dialog, which) -> {
                        getSharedPreferences("no_backup", MODE_PRIVATE)
                                .edit()
                                .putBoolean("askAccessPermission", false)
                                .apply();
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.no, ((dialog, which) -> dialog.dismiss()))
                    .setOnDismissListener(dialog -> showNotificationOffDialog())
                    .show();
        } else {
            showNotificationOffDialog();
        }

        listener =
                (sharedPreferences, key) -> {
                    if (key.equals("last") && appendNotification) {
                        AsyncTask.execute(() -> {
                            NotificationDao.MiniNotification miniNotification
                                    = database.notificationDao().getLastMini();
                            if (miniNotification == null) {
                                runOnUiThread(this::refreshNotifications);
                                return;
                            }
                            if (adapter != null) {
                                runOnUiThread(() -> {
                                    if (isEmpty) {
                                        isEmpty = false;
                                        textView_noHistory.setVisibility(View.GONE);
                                    }
                                    boolean scrollTop = false;
                                    if (layoutManager.findFirstVisibleItemPosition() == 0) {
                                        scrollTop = true;
                                    }
                                    adapter.addNotification(miniNotification);
                                    if (scrollTop) {
                                        layoutManager.smoothScrollToPosition(
                                                recyclerView_notifications, null, 0);
                                    }
                                });
                            }
                        });
                    }
                };

        //add new notifications
        getSharedPreferences("notifier", MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(listener);

        //swipe refresh
        swipeRefresh_notifications.setOnRefreshListener(this::refreshNotifications);
    }
    void showNotificationOffDialog() {
        if (isNotificationChannelEnabled() &&
                getSharedPreferences("no_backup", MODE_PRIVATE).getBoolean(
                        "showNotificationDialog", true)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.turn_off_notification)
                    .setMessage(R.string._turn_off_notification_explanation)
                    .setPositiveButton(R.string.yes, ((dialog, which) -> {
                        Intent intent = new Intent();
                        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                        intent.putExtra("app_package", getPackageName());
                        intent.putExtra("app_uid", getApplicationInfo().uid);
                        intent.putExtra("android.provider.extra.APP_PACKAGE",
                                getPackageName());
                        startActivity(intent);
                        dialog.dismiss();
                    }))
                    .setNeutralButton(getString(R.string.never), (dialog, which) -> {
                        getSharedPreferences("no_backup", MODE_PRIVATE)
                                .edit()
                                .putBoolean("showNotificationDialog", false)
                                .apply();
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.no, ((dialog, which) -> dialog.dismiss()))
                    .setOnDismissListener(dialog -> showAutoStartDialog())
                    .show();
        } else {
            showAutoStartDialog();
        }
    }

    private void showAutoStartDialog() {
        AsyncTask.execute(() -> {
            PackageManager pm = getPackageManager();
            if (pm != null) {
                try {
                    pm.getApplicationInfo("com.miui.securitycenter", 0);
                    if (getSharedPreferences("no_backup", MODE_PRIVATE).getBoolean(
                                    "showAutoStartDialog", true)) {
                        runOnUiThread(() -> new AlertDialog.Builder(this)
                                .setTitle(R.string.enable_autostart)
                                .setMessage(R.string.autostart_explanation)
                                .setPositiveButton(R.string.yes, ((dialog, which) -> {
                                    Intent intent = new Intent();
                                    intent.setComponent(new ComponentName(
                                            "com.miui.securitycenter",
                                            "com.miui.permcenter.autostart" +
                                                    ".AutoStartManagementActivity"));
                                    startActivity(intent);
                                    getSharedPreferences("no_backup", MODE_PRIVATE)
                                            .edit()
                                            .putBoolean("showAutoStartDialog", false)
                                            .apply();
                                    dialog.dismiss();
                                }))
                                .setNeutralButton(getString(R.string.never), (dialog, which) -> {
                                    getSharedPreferences("no_backup", MODE_PRIVATE)
                                            .edit()
                                            .putBoolean("showAutoStartDialog", false)
                                            .apply();
                                    dialog.dismiss();
                                })
                                .setNegativeButton(R.string.no,
                                        ((dialog, which) -> dialog.dismiss()))
                                .setOnDismissListener(dialog -> showAutoStartDialog())
                                .show());
                    }
                } catch (PackageManager.NameNotFoundException ignored) { }
            }
        });
    }

    private boolean isNotificationChannelEnabled() {
        boolean notifEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled();
        if (!notifEnabled) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!TextUtils.isEmpty(ONGOING_CHANNEL_ID)) {
                NotificationManager manager = (NotificationManager)
                        this.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel channel;
                if (manager != null) {
                    channel = manager.getNotificationChannel(ONGOING_CHANNEL_ID);
                    return channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (appendNotification) {
            refreshNotifications();
        }
    }

    private void refreshNotifications() {
        appendNotification = true;
        AsyncTask.execute(() -> {
            adapter.setNotifications(database.notificationDao().getAllMini());
            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                if (adapter.getItemCount() > 0) {
                    textView_noHistory.setVisibility(View.GONE);
                    isEmpty = false;
                } else {
                    textView_noHistory.setVisibility(View.VISIBLE);
                    isEmpty = true;
                }
                swipeRefresh_notifications.setRefreshing(false);
            });
        });
        if (select_date != null) {
            select_date.setIcon(ContextCompat.getDrawable(this,
                    R.drawable.ic_date_range_black_24dp));
        }
    }

    private void refreshNotifications(Calendar date) {
        appendNotification = false;
        AsyncTask.execute(() -> {
            long from = date.getTimeInMillis();
            long to = date.getTimeInMillis();
            to += 86400000;
            adapter.setNotifications(database.notificationDao().getDateMini(from, to));
            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                if (adapter.getItemCount() > 0) {
                    textView_noHistory.setVisibility(View.GONE);
                    isEmpty = false;
                } else {
                    textView_noHistory.setVisibility(View.VISIBLE);
                    isEmpty = true;
                }
                swipeRefresh_notifications.setRefreshing(false);
            });
        });
        if (select_date != null) {
            Drawable icon = getResources().getDrawable(R.drawable.ic_date_range_black_24dp);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                icon.setColorFilter(new BlendModeColorFilter(
                        ContextCompat.getColor(this, R.color.accent), BlendMode.SRC_IN));
            } else {
                icon.setColorFilter(ContextCompat.getColor(this, R.color.accent), SRC_IN);
            }
            select_date.setIcon(icon);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        select_date = menu.findItem(R.id.menu_item_select_date);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_item_refresh:
                refreshNotifications();
                return true;
            case R.id.menu_item_select_date:
                datePickerDialog.show();
                return true;
            case R.id.menu_item_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
