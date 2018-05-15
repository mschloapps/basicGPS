package com.mschloapps.basicgps;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class NavigateToLocation extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];
    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];
    double azimuth = 0;
    GPSLoc curDest;
    GetLocation mService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate_to_location);
        SQLiteAdapter db = new SQLiteAdapter(this);

        final Spinner spinner = findViewById(R.id.spinner);
        final List<GPSLoc> SavedLocs = db.getAllLocations();
        List<String> nms = new ArrayList<>();
        for (int i=0; i<SavedLocs.size(); i++) {
            nms.add(SavedLocs.get(i).getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item ,nms);
        spinner.setAdapter(adapter);
        curDest = SavedLocs.get(spinner.getSelectedItemPosition());

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                curDest = SavedLocs.get(spinner.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("new-location"));

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
    }

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location curLoc = intent.getParcelableExtra("location");
            updateOrientationAngles();
            navToLocation(curLoc, curDest);
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            GetLocation.LocalBinder binder = (GetLocation.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mAccelerometerReading,0, mAccelerometerReading.length);
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mMagnetometerReading,0, mMagnetometerReading.length);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, GetLocation.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("new-location"));
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        mSensorManager.unregisterListener(this);
        super.onStop();
    }

    public void navToLocation (Location loc, GPSLoc dest) {
        Location dst = new Location("");
        dst.setLatitude(dest.getLat());
        dst.setLongitude(dest.getLong());
        dst.setAltitude(dest.getAlt()/3.2808);
        double distTo = loc.distanceTo(dst)*.000621371;
        double distTo_ft = loc.distanceTo(dst)*3.2808;
        DecimalFormat fmt = new DecimalFormat("##.00");
        TextView textView = findViewById(R.id.textView8);
        TextView textView_ft = findViewById(R.id.textView11);
        textView.setText(String.valueOf(fmt.format(distTo)));
        textView_ft.setText(String.valueOf(fmt.format(distTo_ft)));
        double dirTo = -azimuth*360/(2*3.14159f) + loc.bearingTo(dst);
        ImageView imageView = findViewById(R.id.imageView);
        imageView.setRotation((float)dirTo);
    }

    public void updateOrientationAngles() {
        mSensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerometerReading, mMagnetometerReading);
        mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
        azimuth = mOrientationAngles[0];
    }

}
