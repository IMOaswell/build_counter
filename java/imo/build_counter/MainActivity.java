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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    Context mContext;
    static String SHARED_PREFS_KEY = "PACKAGE_NAMES";
    int build_count = 0;
    Uri apkUri;
    Button btn;
    TextView txt;
    Button clearBtn;
    CompoundButton switchBtn;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        Intent intent = getIntent();
        apkUri = getIntent().getData();
        boolean recieveApk = Intent.ACTION_VIEW.equals(intent.getAction());
        
        final ViewGroup btnParent = findViewById(R.id.btn_parent);
        final ViewGroup txtParent = findViewById(R.id.txt_parent);
        switchBtn = findViewById(R.id.switch_btn);
        btn = findViewById(R.id.btn);
        txt = findViewById(R.id.txt);
        clearBtn = findViewById(R.id.clear_btn);
        
        String packageName = "";
        if(recieveApk) packageName = Utils.getApkPackageName(this, apkUri);
        if(!recieveApk){
            //check if theres a sent package name string
            packageName = intent.getStringExtra("packageName");
            if(packageName == null) return;
        }
        populateViewsByPackageName(packageName);
        
        switchBtn.setOnCheckedChangeListener(new OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton switchBtn, boolean isChecked){
                if(isChecked){
                    btnParent.setVisibility(View.GONE);
                    txtParent.setVisibility(View.VISIBLE);
                }
                else{
                    btnParent.setVisibility(View.VISIBLE);
                    txtParent.setVisibility(View.GONE);
                }
            }
        });
    }
    
    void populateViewsByPackageName(final String packageName){
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
        btn.setText(build_count + "");
        txt.setText(sp.getString(COUNT_HISTORY_KEY, "no data yet"));

        btn.setOnClickListener( new OnClickListener(){
                @Override
                public void onClick(View v){
                    build_count++;

                    Button btn = (Button) v;
                    btn.setText(build_count + "");
                    btn.setEnabled(false);

                    Calendar cal = Calendar.getInstance();
                    String recordString = 
                        build_count + " " + 
                        Utils.getCurrentDate(cal) + " " + 
                        Utils.getCurrentTime(cal);

                    sp.edit().putString(LATEST_COUNT_KEY, recordString).apply();
                    sp.edit().putString(COUNT_HISTORY_KEY, sp.getString(COUNT_HISTORY_KEY, "") + "\n" + recordString).apply();
                    Utils.installApk(mContext, apkUri);
                    finish();
                }
            });
            
        clearBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    sp.edit().putString(LATEST_COUNT_KEY, "").apply();
                    sp.edit().putString(COUNT_HISTORY_KEY, "").apply();
                    Toast.makeText(mContext, "successfully cleared history", Toast.LENGTH_LONG).show();
                    populateViewsByPackageName(packageName);
                    switchBtn.setChecked(false);//set all back to default
                }
            });
    }
}
