package se.rende.androidmuter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by dag on 2015-11-15.
 */
public class SettingChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Implement code here to be performed when
        // broadcast is detected
        Log.d("AndroidMuter", "don't disturb setting changed");
    }
}