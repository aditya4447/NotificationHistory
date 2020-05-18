package in.omdev.notificationhistory.database;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import androidx.appcompat.app.AppCompatActivity;
import in.omdev.notificationhistory.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            ((TextView) findViewById(R.id.textView_app_version)).setText(pInfo.versionName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        findViewById(R.id.rate_app_container).setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.developer_url)))));
        findViewById(R.id.open_source_licences_container).setOnClickListener(v ->
                startActivity(new Intent(this, OssLicensesMenuActivity.class)));
    }
}
