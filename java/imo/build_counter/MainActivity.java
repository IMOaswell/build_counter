package imo.build_counter;
 
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
        
        if(!recieveApk) return;
        
        final SharedPreferences sp = getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE);
        final String apkPackageName = getApkPackageName(this, apkUri);
        
        final String KEY = apkPackageName;
        build_count = sp.getInt(KEY, 0);
        
        Button btn = findViewById(R.id.btn);
        btn.setText(apkPackageName + "\n" + build_count);
        
        btn.setOnClickListener( new OnClickListener(){
            @Override
            public void onClick(View v){
                build_count++;
                
                Button btn = (Button) v;
                btn.setText(apkPackageName + "\n" + build_count);
                btn.setEnabled(false);
                
                sp.edit().putInt(KEY, build_count).apply();
                installApk(apkUri);
                finish();
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
}
