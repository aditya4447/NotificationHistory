package in.omdev.notificationhistory.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import in.omdev.notificationhistory.R;
import in.omdev.notificationhistory.database.NotificationDao;



public class NotificationAdapter extends ListAdapter<NotificationDao.MiniNotification,
        NotificationAdapter.NotificationHolder> {

    private List<NotificationDao.MiniNotification> notifications;
    private PackageManager packageManager;
    private MainActivity activity;

    NotificationAdapter(List<NotificationDao.MiniNotification> notifications,
                        NotificationDiff notificationDiff, MainActivity activity) {
        super(notificationDiff);
        this.notifications = notifications;
        packageManager = activity.getPackageManager();
        this.activity = activity;
    }

    public void setNotifications(List<NotificationDao.MiniNotification> allMini) {
        notifications = allMini;
    }

    static class NotificationHolder extends RecyclerView.ViewHolder {

        View root;
        ImageView imageView;
        TextView textView_title;
        TextView textView_date;
        TextView textView_text;
        TextView textView_id;
        TextView textView_package;

        NotificationHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView;
            imageView = root.findViewById(R.id.notification_app_icon);
            textView_title = root.findViewById(R.id.textView_notification_title);
            textView_date = root.findViewById(R.id.textView_notification_date);
            textView_text = root.findViewById(R.id.textView_notification_text);
            textView_id = root.findViewById(R.id.textView_notification_id);
            textView_package = root.findViewById(R.id.textView_notification_package);
        }
    }

    @NonNull
    @Override
    public NotificationAdapter.NotificationHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                     int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        view.setOnClickListener(v -> {

            ImageView imageView_icon = v.findViewById(R.id.notification_app_icon);

            TextView textView_id = v.findViewById(R.id.textView_notification_id);
            int id = Integer.parseInt(textView_id.getText().toString());

            TextView textView_title = v.findViewById(R.id.textView_notification_title);
            String title = textView_title.getText().toString();

            TextView textView_text = v.findViewById(R.id.textView_notification_text);
            String text = textView_text.getText().toString();

            TextView textView_package = v.findViewById(R.id.textView_notification_package);
            String packageName = textView_package.getText().toString();

            Intent intent = new Intent(activity, NotificationActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("id", id);
            intent.putExtra("packageName", packageName);
            intent.putExtra("text", text);
            @SuppressWarnings("unchecked")
            ActivityOptionsCompat activityOptions
                    = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            activity,
                    new Pair<>(textView_title, "title"),
                    new Pair<>(imageView_icon, "icon"),
                    new Pair<>(textView_text, "text"));
            ActivityCompat.startActivity(activity, intent, activityOptions.toBundle());

        });
        return new NotificationHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.NotificationHolder holder,
                                 int position) {
        try {
            holder.imageView.setImageDrawable(
                    packageManager.getApplicationIcon(notifications.get(position).packageName));
        } catch (PackageManager.NameNotFoundException e) {
            holder.imageView.setImageDrawable(packageManager.getDefaultActivityIcon());
        }
        holder.textView_id.setText(String.valueOf(notifications.get(position).id));
        holder.textView_title.setText(notifications.get(position).title);
        holder.textView_date.setText(DateFormat.getDateFormat(activity).format(new Date(notifications.get(position).systemTime)));
        holder.textView_package.setText(notifications.get(position).packageName);
        if (notifications.get(position).textLines != null
                && !notifications.get(position).textLines.equals("null")
                && !notifications.get(position).textLines.trim().equals("")) {
            holder.textView_text.setText(notifications.get(position).textLines);
        } else if (notifications.get(position).bigText != null
                && !notifications.get(position).bigText.equals("null")
                && !notifications.get(position).bigText.trim().equals("")) {
            holder.textView_text.setText(notifications.get(position).bigText);
        } else if (notifications.get(position).text != null
                && !notifications.get(position).text.equals("null")
                && !notifications.get(position).text.trim().equals("")) {
            holder.textView_text.setText(notifications.get(position).text);
        } else {
            holder.textView_text.setText("");
        }
    }

    void addNotification(NotificationDao.MiniNotification notification) {
        notifications.add(0, notification);
        notifyItemInserted(0);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class NotificationDiff
            extends DiffUtil.ItemCallback<NotificationDao.MiniNotification> {

        @Override
        public boolean areItemsTheSame(@NonNull NotificationDao.MiniNotification oldItem,
                                       @NonNull NotificationDao.MiniNotification newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull NotificationDao.MiniNotification oldItem,
                                          @NonNull NotificationDao.MiniNotification newItem) {
            return oldItem.id == newItem.id;
        }
    }
}
