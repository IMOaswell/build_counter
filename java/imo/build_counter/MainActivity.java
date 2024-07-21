package imo.build_counter;
 
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.view.ViewGroup;

public class MainActivity extends Activity {
    String INTERNAL_STORAGE = Environment.getExternalStorageDirectory().getPath();
    final String SHARED_PREFS_KEY = "PACKAGE_NAMES";
    int build_count = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Intent intent = getIntent();
        final Uri apkUri = intent.getData();
        boolean recieveApk = Intent.ACTION_VIEW.equals(intent.getAction());
        final ViewGroup btnParent = findViewById(R.id.btn_parent);
        final ViewGroup txtParent = findViewById(R.id.txt_parent);
        final Button btn = findViewById(R.id.btn);
        final TextView txt = findViewById(R.id.txt);
        final CompoundButton switchBtn = findViewById(R.id.switch_btn);
        
        if(!recieveApk) return;
        
        final SharedPreferences sp = getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE);
        final String apkPackageName = getApkPackageName(this, apkUri);
        
        final String COUNT_HISTORY_KEY = apkPackageName + ":count_history";
        final String LATEST_COUNT_KEY = apkPackageName + ":latest_count";
        
        String recordString = sp.getString(LATEST_COUNT_KEY, "0 ");
        String[] recordStringParts = recordString.split(" ", 2);
        String count = recordStringParts[0];
        String dateAndTime = recordStringParts[1];
        build_count = Integer.parseInt(count.trim());

        setTitle(apkPackageName);
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
                    getCurrentDate(cal) + " " + 
                    getCurrentTime(cal);
                
                sp.edit().putString(LATEST_COUNT_KEY, recordString).apply();
                sp.edit().putString(COUNT_HISTORY_KEY, sp.getString(COUNT_HISTORY_KEY, "") + "\n" + recordString).apply();
                installApk(apkUri);
                finish();
            }
        });
        
        final String switchOff = "BUTTON";
        final String switchOn = "TEXT";
        switchBtn.setText(switchOff);
        switchBtn.setOnCheckedChangeListener(new OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton switchBtn, boolean isChecked){
                if(isChecked){
                    switchBtn.setText(switchOn);
                    btnParent.setVisibility(View.GONE);
                    txtParent.setVisibility(View.VISIBLE);
                }
                else{
                    switchBtn.setText(switchOff);
                    btnParent.setVisibility(View.VISIBLE);
                    txtParent.setVisibility(View.GONE);
                }
            }
        });
    }
    
    
    
    
    
    
	String getApkPackageName(Context mContext,Uri apkUri){
        String filePath = getFilePathFromUri(mContext, apkUri);
        PackageManager pm = mContext.getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(filePath, 0);
        return packageInfo.packageName;
    }
    
    String getFilePathFromUri(Context mContext,Uri apkUri){
        String filePath = null;
        if("file".equals(apkUri.getScheme())){
            filePath = apkUri.getPath();
        }else if("content".equals(apkUri.getScheme())){
            try{
                filePath = copyToTempFile(mContext, apkUri).getAbsolutePath();
            }catch(Exception e){}
        }
        return filePath;
    }
    
    private File copyToTempFile(Context mContext,Uri uri) throws Exception{
        InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
        if(inputStream == null){
            throw new Exception("Failed to open input stream from URI");
        }

        File tempFile = new File(INTERNAL_STORAGE, "temp_apk.apk");
        OutputStream outputStream = new FileOutputStream(tempFile);

        byte[] buffer = new byte[4096];
        int length;
        while((length = inputStream.read(buffer)) > 0){
            outputStream.write(buffer, 0, length);
        }

        inputStream.close();
        outputStream.close();

        return tempFile;
    }
    
    void installApk(Uri apkUri){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }
    
    private String getCurrentDate(Calendar calendar){
        //will return e.g 2024-MAY-19
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd");
        return dateFormat.format(calendar.getTime());
    }

    private String getCurrentTime(Calendar calendar){
        //will return e.g 01:39pm
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mma");
        return dateFormat.format(calendar.getTime());
    }
    
}
