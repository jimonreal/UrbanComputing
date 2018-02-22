package io.github.jimonreal.urbancomputing;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.github.jimonreal.urbancomputing.utils.Log;

public class InfoActivity extends Activity {
    private SensorManager sensorManager;
    private SensorEventThread sensorThread;
    private LocationListener locationListener;
    private LocationManager locationManager;
    //private AppLocationListener appLocationListener;
    //private LocationManager locationManager;

    private final int REPORT_LATENCY_MS = 1 * 1000;
    private final float[] rotationMatrix = new float[9];
    private final float[] inclinationMatrix = new float[9];

    private float[] accelerometerReading = new float[3];
    private float[] magnetometerReading = new float[3];
    private float[] orientation = new float[3];
    private float yaw;
    private float batteryPct;
    private long now;
    private double altitude;
    private double longitude;
    private double latitude;
    private double accuracy;

    private TextView accelerometerX;
    private TextView accelerometerY;
    private TextView accelerometerZ;
    private TextView timeStamp;
    private Button startButton;
    private Button stopButton;

    private Intent batteryStatus;
    private Context context;

    //private long prevEventTime;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorThread = new SensorEventThread("Sensor Thread");

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    altitude = location.getAltitude();
                    accuracy = location.getAccuracy();

                    //cleanUp();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }

            public void cleanUp() {
                if (locationListener != null && locationManager != null) {
                    locationManager.removeUpdates(locationListener);
                    locationListener = null;
                }
            }
        };

        //appLocationListener = new AppLocationListener();
        //locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        handler = new Handler();
        context = this.getApplicationContext();

        accelerometerX = findViewById(R.id.accelerometerXValue);
        accelerometerY = findViewById(R.id.accelerometerYValue);
        accelerometerZ = findViewById(R.id.accelerometerZValue);
        timeStamp = findViewById(R.id.timeBox);

        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);

        stopButton.setVisibility(View.GONE);

        now = System.currentTimeMillis();

        //prevEventTime = System.currentTimeMillis();
    }

    public void updateActivityScreen() {
        accelerometerX.setText(String.valueOf(accelerometerReading[0]));
        accelerometerY.setText(String.valueOf(accelerometerReading[1]));
        accelerometerZ.setText(String.valueOf(accelerometerReading[2]));
        timeStamp.setText(String.valueOf(now));
    }

    public void startCommand(View v) {
        startSensing();
    }

    public void startSensing() {
        // We get data from the sensor every REPORT_LATENCY_US / 1000000 seconds.
        sensorManager.registerListener(sensorThread,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL,
                sensorThread.getHandler2());

        sensorManager.registerListener(sensorThread,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL,
                sensorThread.getHandler2());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        startButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.VISIBLE);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.sensorDataToCSV(context,
                        accelerometerReading,
                        latitude,
                        longitude,
                        altitude,
                        accuracy,
                        batteryPct,
                        now);
                updateActivityScreen();


                runnable = this;
                handler.postDelayed(runnable, REPORT_LATENCY_MS);
            }
        }, REPORT_LATENCY_MS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1001: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                    //Start your service here
                }
            }
        }
    }

    public void stopCommand(View v) {
        stopSensing();
    }

    public void stopSensing() {
        sensorManager.unregisterListener(sensorThread);
        locationManager.removeUpdates(locationListener);
        sensorThread.quitLooper();
        handler.removeCallbacksAndMessages(null);

        startButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.GONE);
    }


    class SensorEventThread extends HandlerThread implements SensorEventListener {

        Handler handler2;

        public SensorEventThread(String name) {
            super(name);
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            now = System.currentTimeMillis();
            //if (Math.abs(now - prevEventTime) >= REPORT_LATENCY_MS) {
            int sensorType = sensorEvent.sensor.getType();
            //ACELEROMETER
            //We store the values of the accelerometer in our reading vector
            if (sensorType == Sensor.TYPE_ACCELEROMETER) {
                accelerometerReading = sensorEvent.values;

            } else if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
                magnetometerReading = sensorEvent.values;
            }

            if (magnetometerReading != null && accelerometerReading != null) {
                if (
                        SensorManager.getRotationMatrix(rotationMatrix,
                                inclinationMatrix,
                                accelerometerReading,
                                magnetometerReading)) {

                    float[] prevOrientation = orientation.clone();
                    orientation = SensorManager.getOrientation(rotationMatrix, orientation);
                    yaw = orientation[0] - prevOrientation[0];
                }
            }

            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            batteryStatus = context.registerReceiver(null, iFilter);

            //BATTERY LEVEL
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryPct = level / (float)scale;
            //prevEventTime = now;
            //}
        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            handler2 = new Handler(sensorThread.getLooper());
        }

        public Handler getHandler2() {
            return handler2;
        }

        public void quitLooper() {
            if (sensorThread.isAlive()) {
                sensorThread.getLooper().quit();
            }
        }

    }
/*
    class AppLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                altitude = location.getAltitude();
                accuracy = location.getAccuracy();

                cleanUp();
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }

        public void cleanUp() {
            if (appLocationListener != null) {

            }
        }
    }
*/

}
