package se.rende.androidmuter;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "MyActivity";
    private SensorManager mSensorManager;
    private Sensor mProximity;
    private Sensor mAccelerometer;
    private AccelerometerListener accelerometerListener = new AccelerometerListener();
    private NotificationManager mNotificationManager;
    private int isFaceDownSeconds;
    private Switch mMuterActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mMuterActive = (Switch)findViewById(R.id.muterActive);

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mMuterActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("AndroidMuter", "click " + mMuterActive.isChecked());
                if (mMuterActive.isChecked()) {
                    setMuterActive();
                } else {
                    setMuterInactive();
                }
            }
        });

        if (mNotificationManager.isNotificationPolicyAccessGranted()) {
            mMuterActive.setChecked(true);
            setMuterActive();
        } else {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Enable Don't disturb change")
                    .setMessage("You have to change a setting for this app to work.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), 0);
                        }})
                    .setNegativeButton(android.R.string.cancel, null).show();
        }

    }

    private void setMuterInactive() {
        Log.d("AndroidMuter", "set muter inactive");
        mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
        mSensorManager.unregisterListener(accelerometerListener);
        mSensorManager.unregisterListener(MainActivity.this);
    }

    private void setMuterActive() {
        Log.d("AndroidMuter", "set muter active");
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorManager.registerListener(MainActivity.this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            Log.d("AndroidMuter", "prox change to " + event.values[0]);
            if (event.values[0] < 0.1) {
                // front is against something - don't disturb
                mSensorManager.registerListener(accelerometerListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                // not against something - may disturb again
                Log.d("AndroidMuter", "not silent");
                mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
                mSensorManager.unregisterListener(accelerometerListener);
            }
        }
    }

    class AccelerometerListener implements SensorEventListener {
        private long isFaceDownStartTime;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // test if face down
//                Log.d("AndroidMuter", "accel change to x=" + event.values[0] + " y=" + event.values[1] + " z=" + event.values[2]);
                boolean isFaceDown = Math.abs(event.values[0]) < 0.6
                        && Math.abs(event.values[1]) < 0.6
                        && Math.abs(event.values[2] + 9.81) < 1.0;

                if (isFaceDown) {
                    if ((event.timestamp - isFaceDownStartTime) > (5 * 1000000000L)) {
                        Log.d("AndroidMuter", "silent");
                        mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS);
                        mSensorManager.unregisterListener(accelerometerListener);
                    }
                } else {
                    isFaceDownStartTime = event.timestamp;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
