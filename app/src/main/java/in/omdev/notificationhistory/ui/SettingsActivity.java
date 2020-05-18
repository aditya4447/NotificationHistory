package in.omdev.notificationhistory.ui;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.preference.DropDownPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.room.Room;
import in.omdev.notificationhistory.R;
import in.omdev.notificationhistory.component.ListenerService;
import in.omdev.notificationhistory.database.NotificationDatabase;
import in.omdev.notificationhistory.database.NotificationEntity;

import static in.omdev.notificationhistory.Const.IMPORT_FILE_REQUEST;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment(this))
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private SettingsActivity activity;
        private NotificationDatabase database;
        private Gson gson = new Gson();
        private Preference preference_import;
        private Preference preference_count;

        public SettingsFragment() {}

        public SettingsFragment(SettingsActivity settingsActivity) {
            activity = settingsActivity;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            if (activity == null) {
                activity = (SettingsActivity) getActivity();
            }
            if (activity == null) {
                return;
            }
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            //ongoing notifications setting
            SwitchPreferenceCompat switch_ongoing = findPreference("insertOngoing");
            if (switch_ongoing != null) {
                switch_ongoing.setOnPreferenceChangeListener((preference, newValue) -> {
                    ListenerService.insertOngoing =
                            (boolean) newValue;
                    return true;
                });
            }

            //maximum entries setting
            DropDownPreference dropDownPreference_maxEntries = findPreference("maxEntries");
            if (dropDownPreference_maxEntries != null) {
                dropDownPreference_maxEntries.setSummary(
                        getString(R.string.summary_max_entries,
                                dropDownPreference_maxEntries.getValue()));
                dropDownPreference_maxEntries.setOnPreferenceChangeListener(
                        (preference, newValue) -> {
                            dropDownPreference_maxEntries.setSummary(
                                    getString(R.string.summary_max_entries,
                                            newValue));
                            ListenerService.maxEntries = Integer.parseInt(String.valueOf(newValue));
                            return true;
                        });
            }

            //entries count
            preference_count = findPreference("count");
            if (preference_count != null) {
                initDatabase();
                AsyncTask.execute(() -> {
                    int entries = database.notificationDao().getRowCount();
                    activity.runOnUiThread(() ->
                            preference_count.setSummary(String.valueOf(entries)));
                });
            }

            //clear notification database
            Preference preference_clear = findPreference("clear");
            if (preference_clear != null) {
                preference_clear.setOnPreferenceClickListener(preference -> {
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.clear)
                            .setMessage(R.string._confirm_clear)
                            .setPositiveButton(R.string.yes, (dialog, which) -> {
                                AsyncTask.execute(() -> database.notificationDao().deleteAll());
                                if (preference_count != null) {
                                    preference_count.setSummary("0");
                                }
                            })
                            .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                            .show();
                    return true;
                });
            }

            //export
            Preference preference_export = findPreference("export");
            if (preference_export != null) {
                preference_export.setOnPreferenceClickListener(preference -> {
                    preference_export.setSummary(R.string._exporting);
                    AsyncTask.execute(() -> {
                        NotificationEntity[] notifications =
                                database.notificationDao().getAll()
                                        .toArray(new NotificationEntity[0]);
                        try {
                            File file = new File(activity.getExternalFilesDir(null),
                                    "notification_history_"
                                            + System.currentTimeMillis() + ".json");
                            FileWriter writer = new FileWriter(file);
                            gson.toJson(notifications, writer);
                            writer.flush();
                            writer.close();
                            activity.runOnUiThread(() -> {
                                preference_export.setSummary(R.string.file_exported);
                                Uri uri = FileProvider.getUriForFile(
                                        activity,
                                        "in.omdev.fileprovider",
                                        file);
                                ShareCompat.IntentBuilder.from(activity)
                                        .setStream(uri)
                                        .setType("application/json")
                                        .startChooser();
                            });
                        } catch (Exception e) {
                            activity.runOnUiThread(() ->
                                    preference_export.setSummary(R.string.error_exporting_file));
                        }
                    });
                    return true;
                });
            }
            preference_import = findPreference("import");
            if (preference_import != null) {
                preference_import.setOnPreferenceClickListener(preference -> {
                    preference_import.setSummary(R.string._select_file);
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/octet-stream");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, IMPORT_FILE_REQUEST);
                    return true;
                });
            }

            //notification access permission
            Preference preference_permission = findPreference("permission");
            if (preference_permission != null) {
                preference_permission.setOnPreferenceClickListener(preference -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        activity.startActivity(new Intent(
                                Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    } else {
                        activity.startActivity(new Intent(
                                "android.settings" +
                                        ".ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    }
                    return true;
                });
            }

            //notification settings
            Preference preference_notification = findPreference("notification");
            if (preference_notification != null) {
                preference_notification.setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent();
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    intent.putExtra("app_package", activity.getPackageName());
                    intent.putExtra("app_uid", activity.getApplicationInfo().uid);
                    intent.putExtra("android.provider.extra.APP_PACKAGE",
                            activity.getPackageName());
                    activity.startActivity(intent);
                    return true;
                });
            }
        }

        private void initDatabase() {
            if (database == null) {
                database = Room.databaseBuilder(
                        activity.getApplicationContext(),
                        NotificationDatabase.class, "notification_history").build();
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == IMPORT_FILE_REQUEST) {
                if (resultCode != RESULT_OK) {
                    preference_import.setSummary(R.string._summary_import);
                    return;
                }
                if (data != null && data.getData() != null) {
                    preference_import.setSummary(R.string._importing);
                    AsyncTask.execute(() -> {
                        try {
                            Gson gson = new Gson();
                            ContentResolver resolver = activity.getContentResolver();
                            if (resolver == null)
                                throw new Exception("Content resolver null");
                            InputStream inputStream = resolver.openInputStream(data.getData());
                            if (inputStream == null)
                                throw new Exception("Input stream null");
                            BufferedReader reader = new BufferedReader(new InputStreamReader(
                               inputStream
                            ));
                            NotificationEntity[] notifications =
                                    gson.fromJson(reader, NotificationEntity[].class);
                            for (NotificationEntity notification : notifications) {
                                notification.id = 0;
                                database.notificationDao().insertAll(notification);
                            }
                            int new_total = database.notificationDao().getRowCount();
                            activity.runOnUiThread(() -> {
                                preference_import.setSummary(R.string.import_complete);
                                if (preference_count != null) {
                                    preference_count.setSummary(String.valueOf(new_total));
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (preference_import != null) {
                                activity.runOnUiThread(() ->
                                        preference_import.setSummary(R.string._import_error));
                            }
                        }
                    });
                } else {
                    preference_import.setSummary(R.string._summary_import);
                }
            }
        }
    }
}