package imo.build_counter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PackagePickerActivity extends Activity {
    Context mContext;
    String SHARED_PREFS_KEY = MainActivity.SHARED_PREFS_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_picker);
        mContext = this;
        final SharedPreferences sp = getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE);
        List<String> spKeys = new ArrayList<>(sp.getAll().keySet());
        Set<String> packageNames = new HashSet<String>();

        for(String s : spKeys) {
            String packageName = s.split(":")[0];
            packageNames.add(packageName);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            this, android.R.layout.simple_list_item_1, 
            new ArrayList<String>(packageNames));

        ListView listview = findViewById(R.id.listview);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String clickedItem = (String) parent.getItemAtPosition(position);
                    String packageName = clickedItem;

                    Intent intent = new Intent(PackagePickerActivity.this, MainActivity.class);
                    intent.putExtra("packageName", packageName);
                    startActivity(intent);
                }
            });

            
        final File buildCounterTxt = new File(Utils.INTERNAL_STORAGE, "AppProjects/build_counter.txt");
        
        Button exportBtn = findViewById(R.id.export_btn);
        exportBtn = Utils.underline(exportBtn);
        exportBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                String fileContent = genBuildCounterTxtString(sp);
                
                Utils.write(buildCounterTxt, fileContent);
                Toast.makeText(mContext, "successfully exported data as file at:\n" + buildCounterTxt.getPath(), Toast.LENGTH_LONG).show();
            }
        });
        
        Button importBtn = findViewById(R.id.import_btn);
        importBtn = Utils.underline(importBtn);
        importBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    String fileContent = Utils.read(buildCounterTxt.getPath());
                    importStringToSharedPref(sp, fileContent);
                    Toast.makeText(mContext, "successfully imported data from file:\n" + buildCounterTxt.getPath(), Toast.LENGTH_LONG).show();
                }
            });
    }
    
    String genBuildCounterTxtString(SharedPreferences sp){
        StringBuilder sb = new StringBuilder();
        
        List<String> spKeys = new ArrayList<>(sp.getAll().keySet());
        Set<String> packageNames = new HashSet<String>();
        for(String s : spKeys) {
            String packageName = s.split(":")[0];
            packageNames.add(packageName);
        }
        
        for(String packageName : packageNames){
            final String COUNT_HISTORY_KEY = packageName + ":count_history";
            sb.append("\n@" + packageName + "\n");
            sb.append(sp.getString(COUNT_HISTORY_KEY, "").trim());
        }
        return sb.toString();
    }
    
    void importStringToSharedPref(SharedPreferences sp, String input){
        String[] packageNamesWithLogs = input.split("@");
        for(String string : packageNamesWithLogs){
            if(string.isEmpty()) continue;
            String[] stringParts = string.split("\n", 2);
            
            String packageName = stringParts[0];
            final String COUNT_HISTORY_KEY = packageName + ":count_history";
            final String LATEST_COUNT_KEY = packageName + ":latest_count";
            
            String logsRaw = stringParts[1];
            String[] logs = logsRaw.split("\n");
            String lastLog = logs[logs.length - 1];
            
            sp.edit().putString(COUNT_HISTORY_KEY, logsRaw).apply();
            sp.edit().putString(LATEST_COUNT_KEY, lastLog).apply();
        }
    }
}
