package imo.build_counter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

public class MainActivity extends Activity {
    TextView recordBtnHint;
    TextView historyTxt;
    Button clearHistoryBtn;
    Button switchTabBtn;
    Button recordBtn;
    
    static String SHARED_PREFS_KEY = "PACKAGE_NAMES";
    boolean isViewHistoryTab = false;
    int build_count = 0;
    Context mContext;
    Bundle mBundle;
    Uri apkUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mBundle == null) mBundle = savedInstanceState;
        if(!Utils.hasPermissions(this)){
            Utils.requestPermission(this);
            return;
        }
        
        setContentView(R.layout.activity_main);
        mContext = this;
        
        final ViewGroup recordBtnParent = findViewById(R.id.record_btn_parent);
        final ViewGroup historyTxtParent = findViewById(R.id.history_txt_parent);
        switchTabBtn = findViewById(R.id.switch_tab_btn);
        recordBtnHint = findViewById(R.id.record_btn_hint);
        recordBtn = findViewById(R.id.record_btn);
        historyTxt = findViewById(R.id.history_txt);
        clearHistoryBtn = findViewById(R.id.history_clear_btn);
        
        Intent intent = getIntent();
        apkUri = getIntent().getData();
        boolean recieveApk = Intent.ACTION_VIEW.equals(intent.getAction());

        String packageName = "";
        if(recieveApk) packageName = Utils.getApkPackageName(this, apkUri);
        if(!recieveApk) packageName = intent.getStringExtra("packageName");
        if(packageName == null) return;

        init(packageName);

        switchTabBtn.setText("VIEW HISTORY");
        switchTabBtn = Utils.underline(switchTabBtn);
        switchTabBtn.setOnClickListener(new OnClickListener(){
                @Override 
                public void onClick(View v) {
                    if(isViewHistoryTab) {
                        isViewHistoryTab = false;
                        switchTabBtn.setText("VIEW HISTORY");
                        recordBtnParent.setVisibility(View.VISIBLE);
                        historyTxtParent.setVisibility(View.GONE);

                    } else {
                        isViewHistoryTab = true;
                        switchTabBtn.setText("BACK");
                        recordBtnParent.setVisibility(View.GONE);
                        historyTxtParent.setVisibility(View.VISIBLE);

                        String txtString = historyTxt.getText().toString().trim();
                        clearHistoryBtn.setVisibility(txtString.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                }
            });
    }

    void init(final String packageName) {
        final SharedPreferences sp = getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE);
        final String COUNT_HISTORY_KEY = packageName + ":count_history";
        final String LATEST_COUNT_KEY = packageName + ":latest_count";

        String recordString = sp.getString(LATEST_COUNT_KEY, "");
        if(recordString.isEmpty()) recordString = "0 ";
        String[] recordStringParts = recordString.split(" ", 2);
        String count = recordStringParts[0];
        String dateAndTime = recordStringParts[1];
        build_count = Integer.parseInt(count.trim());

        setTitle(packageName);
        recordBtn.setText(build_count + "");
        recordBtnHint.setText(R.string.record_count_hint);
        historyTxt.setText(sp.getString(COUNT_HISTORY_KEY, "no data yet"));

        recordBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    build_count++;

                    Calendar cal = Calendar.getInstance();
                    String recordString = 
                        build_count + " " + 
                        Utils.getCurrentDate(cal) + " " + 
                        Utils.getCurrentTime(cal);

                    sp.edit().putString(LATEST_COUNT_KEY, recordString).apply();
                    sp.edit().putString(COUNT_HISTORY_KEY, sp.getString(COUNT_HISTORY_KEY, "").trim() + "\n" + recordString).apply();
                    
                    historyTxt.setText(sp.getString(COUNT_HISTORY_KEY, "no data yet"));
                    recordBtn.setText(build_count + "");
                    recordBtn.setEnabled(false);
                    recordBtn.setTextColor(Color.WHITE);
                    recordBtn.setAlpha(0.5f);

                    Toast.makeText(mContext, R.string.record_count_success, Toast.LENGTH_LONG).show();
                    if(apkUri != null) Utils.installApk(mContext, apkUri);

                }
            });

        clearHistoryBtn = Utils.underline(clearHistoryBtn);
        clearHistoryBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    sp.edit().putString(LATEST_COUNT_KEY, "").apply();
                    sp.edit().putString(COUNT_HISTORY_KEY, "").apply();

                    Toast.makeText(mContext, R.string.clear_history_success, Toast.LENGTH_LONG).show();
                    init(packageName);
                    switchTabBtn.performClick();// switch back to the other tab
                }
            });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(Utils.hasPermissions(this)) {
            onCreate(mBundle);
            return;
        }
        finish();
    }
}
