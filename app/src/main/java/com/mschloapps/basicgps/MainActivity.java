package com.mschloapps.basicgps;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    String[] AppPermissions = {Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int PERMISSIONS_REQUEST_ALL = 1;
    GetLocation mService;
    private boolean mBound = false;
    private SQLiteAdapter db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                storeLoc();
            }
        });

        final Button nav_button = findViewById(R.id.nav_button);
        nav_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long numLocs = db.getNumLocations();
                if (numLocs > 0) {
                    gotoNav();
                } else {
                    Toast.makeText(getApplicationContext(), "No Locations Stored.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final Button edit_button = findViewById(R.id.button2);
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoEdit();
            }
        });

        final Switch man_mode_sw = findViewById(R.id.switch1);
        man_mode_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                EditText latitude = findViewById(R.id.editText2);
                EditText longitude = findViewById(R.id.editText3);
                EditText altitude = findViewById(R.id.editText5);
                if (man_mode_sw.isChecked()){
                    LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(br);
                    latitude.setEnabled(true);
                    latitude.setCursorVisible(true);
                    longitude.setEnabled(true);
                    longitude.setCursorVisible(true);
                    altitude.setEnabled(true);
                    altitude.setCursorVisible(true);
                } else {
                    latitude.setEnabled(false);
                    latitude.setCursorVisible(false);
                    longitude.setEnabled(false);
                    longitude.setCursorVisible(false);
                    altitude.setEnabled(false);
                    altitude.setCursorVisible(false);
                    LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(br, new IntentFilter("new-location"));
                }
            }
        });

        db = new SQLiteAdapter(this);
    }

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location curLoc = intent.getParcelableExtra("location");
            updateLocation(curLoc);
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

    public void gotoNav(){
        Intent intent = new Intent(this, NavigateToLocation.class);
        startActivity(intent);
    }

    public void gotoEdit(){
        Intent intent = new Intent(this, EditLocations.class);
        startActivity(intent);
    }

    public boolean checkPermissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String p : permissions) {
                if (ActivityCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!checkPermissions(this, AppPermissions)){
            ActivityCompat.requestPermissions( this, AppPermissions, PERMISSIONS_REQUEST_ALL);
        } else {
            Intent intent = new Intent(this, GetLocation.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("new-location"));
        }
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    public void updateLocation(Location loc) {
        EditText latitude = findViewById(R.id.editText2);
        EditText longitude = findViewById(R.id.editText3);
        EditText direction = findViewById(R.id.editText4);
        EditText altitude = findViewById(R.id.editText5);
        EditText speed = findViewById(R.id.editText6);
        EditText speed_mph = findViewById(R.id.editText);
        DecimalFormat fmt1 = new DecimalFormat("##.00000");
        DecimalFormat fmt2 = new DecimalFormat("##.00");

        latitude.setText(String.valueOf(fmt1.format(loc.getLatitude())));
        longitude.setText(String.valueOf(fmt1.format(loc.getLongitude())));
        direction.setText(String.valueOf(fmt2.format(loc.getBearing())));
        altitude.setText(String.valueOf(fmt2.format(loc.getAltitude()*3.2808)));
        speed.setText(String.valueOf(fmt2.format(loc.getSpeed()*3.2808)));
        speed_mph.setText(String.valueOf(fmt2.format(loc.getSpeed()*2.2369)));
    }

    public void storeLoc () {
        final String emptyStr = "";
        double lat = 0.0000;
        EditText latitude = findViewById(R.id.editText2);
        if (emptyStr.contentEquals(latitude.getText())) {
            Toast.makeText(this, "Enter Latitude.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            lat = Double.parseDouble(latitude.getText().toString());
        }

        double lng = 0.0000;
        EditText longitude = findViewById(R.id.editText3);
        if (emptyStr.contentEquals(longitude.getText())) {
            Toast.makeText(this, "Enter Longitude.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            lng = Double.parseDouble(longitude.getText().toString());
        }

        double alt = 0.0000;
        EditText altitude = findViewById(R.id.editText5);
        if (emptyStr.contentEquals(altitude.getText())) {
            Toast.makeText(this, "Enter Altitude.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            alt = Double.parseDouble(altitude.getText().toString());
        }

        EditText name = findViewById(R.id.editText7);
        String locName;
        if (emptyStr.contentEquals(name.getText())) {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            String dt = fmt.format(new Date());
            locName = "Loc_" + dt;
        } else {
            locName = name.getText().toString();
        }

        if (db.getNumLocations() > 0) {
            if (db.nameExists(locName)) {
                Toast.makeText(this, "Location name already exists.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        GPSLoc loc = new GPSLoc(locName, lat, lng, alt);
        long res = db.addLoc(loc);
        if (res == -1) {
            Toast.makeText(this, "Error saving location.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Location saved.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ALL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, GetLocation.class);
                    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                    LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("new-location"));
                } else {
                    if (mBound) {
                        LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
                        unbindService(mConnection);
                        mBound = false;
                    }
                }
            }
        }
    }
}
