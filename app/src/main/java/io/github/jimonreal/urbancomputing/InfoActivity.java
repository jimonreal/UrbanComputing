package io.github.jimonreal.urbancomputing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class InfoActivity extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private final int REPORT_LATENCY_US = 10 * 1000000;
    private final float[] accelerometerReading = new float[3];
    private TextView accelerometerX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        accelerometerX = findViewById(R.id.accelerometerXValue);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // We get data from the sensor every REPORT_LATENCY_US / 1000000 seconds.
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL,
                REPORT_LATENCY_US);
    }

    @Override
    protected  void onPause(){
        super.onPause();

        //We stop receiving updates from either sensor.
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //We store the values of the accelerometer in our reading vector
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(sensorEvent.values,
                    0,
                    accelerometerReading,
                    0,
                    accelerometerReading.length);
        }
        updateActivityScreen();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //If accuracy change we are not doing anything!
    }

    public void updateActivityScreen() {
        accelerometerX.setText(String.valueOf(accelerometerReading[0]));
        System.out.println("Updating the value");
    }
}
