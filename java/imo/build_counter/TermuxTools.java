package imo.build_counter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.InputType;
import android.widget.EditText;
import android.content.DialogInterface;

public class TermuxTools{
    static boolean hasPermission(Activity activity){
        return activity.checkSelfPermission("com.termux.permission.RUN_COMMAND") == PackageManager.PERMISSION_GRANTED;
    }

    static void requestPermission(Activity activity){
        activity.requestPermissions(new String[]{"com.termux.permission.RUN_COMMAND"}, 69);
    }
    
    static void showRunScriptDialog(final Activity mActivity, final Runnable onRunScript){
        final Context mContext = mActivity;
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        
        final EditText input = new EditText(mContext);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setTextColor(mActivity.getColor(R.color.text_color));
        
        builder.setView(input);
        builder.setTitle("Run Script on Termux");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                    if(onRunScript != null) onRunScript.run();
                    dialog.cancel();
                }
            });

        builder.setPositiveButton("Run", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                    if(onRunScript != null) onRunScript.run();
                    if(!hasPermission(mActivity)){
                        requestPermission(mActivity);
                        dialog.cancel();
                        showRunScriptDialog(mActivity, onRunScript);
                        return;
                    }
                    runScript(mContext, input.getText().toString());
                }
            });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.color.primary);
        dialog.show();
    }

    static void runScript(Context context, String script){
        Intent intent = new Intent();
        intent.setClassName("com.termux", "com.termux.app.RunCommandService");
        intent.setAction("com.termux.RUN_COMMAND");
        intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/sh");
        intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[]{"-c", script});
        context.startService(intent);
    }
}
