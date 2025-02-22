package imo.build_counter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import java.io.InputStream;

public class DebugActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String errMsg = "";
		if(intent != null) errMsg = intent.getStringExtra("error");
		
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
		bld.setTitle("An error occured");
		bld.setMessage( errMsg );
		bld.setNeutralButton("End Application", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		bld.create().show();
    }
}
