package imo.build_counter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Environment;
import android.widget.Button;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {
    static String INTERNAL_STORAGE = Environment.getExternalStorageDirectory().getPath();

    static String getApkPackageName(Context mContext, Uri apkUri) {
        String filePath = getFilePathFromUri(mContext, apkUri);
        PackageManager pm = mContext.getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(filePath, 0);
        return packageInfo.packageName;
    }

    static String getFilePathFromUri(Context mContext, Uri apkUri) {
        String filePath = null;
        if("file".equals(apkUri.getScheme())) {
            filePath = apkUri.getPath();
        } else if("content".equals(apkUri.getScheme())) {
            try {
                filePath = copyToTempFile(mContext, apkUri).getAbsolutePath();
            } catch(Exception e) {}
        }
        return filePath;
    }

    private static File copyToTempFile(Context mContext, Uri uri) throws Exception {
        InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
        if(inputStream == null) {
            throw new Exception("Failed to open input stream from URI");
        }

        File tempFile = new File(INTERNAL_STORAGE, "temp_apk.apk");
        OutputStream outputStream = new FileOutputStream(tempFile);

        byte[] buffer = new byte[4096];
        int length;
        while((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        inputStream.close();
        outputStream.close();

        return tempFile;
    }

    static void installApk(Context mContext, Uri apkUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        mContext.startActivity(intent);
    }

    static String getCurrentDate(Calendar calendar) {
        //will return e.g 2024-MAY-19
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd");
        return dateFormat.format(calendar.getTime());
    }

    static String getCurrentTime(Calendar calendar) {
        //will return e.g 01:39pm
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mma");
        return dateFormat.format(calendar.getTime());
    }
    
    static void write(File file,String input){
        try{
            if(!file.exists()) file.createNewFile();
            if(file.isFile()){
                FileWriter fw = new FileWriter(file);
                fw.write(input);
                fw.close();
            }
        }catch(IOException e){}
    }
    
    static String read(String filePath){
        StringBuilder content = new StringBuilder();

        try{
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;

            while((line = reader.readLine()) != null){
                content.append(line).append("\n");
            }
            reader.close();
        }catch(IOException e){}
        return content.toString().trim();
    }
    
    static Button underline(Button btn){
        btn.setPaintFlags(btn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        return btn;
    }
}
