package in.omdev.notificationhistory.database;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class NotificationEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "notification_id")
    public int notificationId;

    public String key;

    @ColumnInfo(name = "package_name")
    public String packageName;

    @ColumnInfo(name = "system_time", index = true)
    public long systemTime;

    @ColumnInfo(name = "post_time")
    public long postTime;

    public long when;

    public String title;

    public String text;

    @ColumnInfo(name = "big_text")
    public String bigText;

    @ColumnInfo(name = "text_lines")
    public String textLines;

    @ColumnInfo(name = "ticker_text")
    public String tickerText;

    public String tag;

    @ColumnInfo(name = "is_ongoing")
    public boolean isOngoing;

    public int color;

    public int visibility;

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof NotificationEntity && ((NotificationEntity) obj).id == id;
    }
}
