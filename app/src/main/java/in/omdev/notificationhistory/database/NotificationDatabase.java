package in.omdev.notificationhistory.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {NotificationEntity.class}, version = 1, exportSchema = false)
public abstract class NotificationDatabase extends RoomDatabase {
    public abstract NotificationDao notificationDao();
}
