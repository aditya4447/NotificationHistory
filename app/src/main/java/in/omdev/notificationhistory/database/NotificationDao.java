package in.omdev.notificationhistory.database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY system_time DESC")
    List<NotificationEntity> getAll();

    @Query("SELECT id"
            + ", package_name AS packageName"
            + ", system_time AS systemTime"
            + ", title"
            + ", text"
            + ", big_text AS bigText"
            + ", text_lines AS textLines"
            + " FROM notifications ORDER BY system_time DESC")
    List<MiniNotification> getAllMini();

    @Query("SELECT id"
            + ", package_name AS packageName"
            + ", system_time AS systemTime"
            + ", title"
            + ", text"
            + ", big_text AS bigText"
            + ", text_lines AS textLines"
            + " FROM notifications ORDER BY system_time DESC LIMIT 1")
    MiniNotification getLastMini();

    @Query("SELECT id"
            + ", package_name AS packageName"
            + ", system_time AS systemTime"
            + ", title"
            + ", text"
            + ", big_text AS bigText"
            + ", text_lines AS textLines"
            + " FROM notifications WHERE system_time >= :from AND system_time < :to"
            + " ORDER BY system_time DESC")
    List<MiniNotification> getDateMini(long from, long to);

    @Query("SELECT * FROM notifications WHERE id=:id")
    NotificationEntity get(int id);

    @Query("SELECT COUNT(id) FROM notifications")
    int getRowCount();

    @Query("DELETE FROM notifications WHERE id in (SELECT id FROM notifications ORDER BY system_time ASC LIMIT :rows)")
    void deleteLast(int rows);

    @Insert
    void insertAll(NotificationEntity... notifications);

    @Delete
    void delete(NotificationEntity notification);

    @Query("DELETE FROM notifications")
    void deleteAll();

    class MiniNotification {
        public int id;
        public String packageName;
        public long systemTime;
        public String title;
        public String text;
        public String bigText;
        public String textLines;
    }
}
