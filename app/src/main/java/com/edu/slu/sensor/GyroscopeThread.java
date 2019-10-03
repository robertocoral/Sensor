package com.edu.slu.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Thread that registers, unregisters the Gyroscope and processes the Gyroscope data
 * Created by sashank on 1/22/15.
 */
public class GyroscopeThread extends Thread implements SensorEventListener {
    private Context context = null;
    private SensorManager sensor_manager = null;

    /**
     * Default constructor
     * @param context
     */
    public GyroscopeThread(Context context) {
        this.context = context;
    }

    /**
     * Register the gyroscope sensor
     */
    @Override
    public void run() {
        super.run();

        // Obtain an instance of the sensor service and register the gyroscope sensor
        sensor_manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor_manager.registerListener(this, sensor_manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), 100000);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        GyroscopeObject gyroscope_obj = new GyroscopeObject(System.currentTimeMillis(), event.accuracy, event.values[0], event.values[1], event.values[2]);
        // Display the gyroscope data
        gyroscope_obj.display();
        // Write the gyroscope data if the recording has started
        if (MainActivity.is_recording) {
            gyroscope_obj.write();
        }

        // Check if the thread has been interrupted
        if (this.isInterrupted()) {
            // Request cancellation for updates on gyroscope data
            sensor_manager.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
