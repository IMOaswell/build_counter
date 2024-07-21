package imo.build_counter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import imo.build_counter.MainActivity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PackagePickerActivity extends Activity {
    
    String SHARED_PREFS_KEY = MainActivity.SHARED_PREFS_KEY;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_picker);
        final SharedPreferences sp = getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE);
        List<String> spKeys = new ArrayList<>(sp.getAll().keySet());
        Set<String> packageNames = new HashSet<String>();
        
        for (String s : spKeys) {
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
    }
    
}
