package com.edu.slu.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Thread that registers, unregisters the Accelerometer and processes the Accelerometer data
 * Created by sashank on 1/22/15.
 */
public class AccelerometerThread extends Thread implements SensorEventListener {
    private Context context = null;
    private SensorManager sensor_manager = null;

    /**
     * Default constructor
     * @param context
     */
    public AccelerometerThread(Context context) {
        this.context = context;
    }

    /**
     * Register the accelerometer sensor
     */
    @Override
    public void run() {
        super.run();

        // Obtain an instance of the sensor service and register the accelerometer sensor
        sensor_manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor_manager.registerListener(this, sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 100000);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        AccelerometerObject accelerometer_obj = new AccelerometerObject(System.currentTimeMillis(), event.accuracy, event.values[0], event.values[1], event.values[2]);
        // Display the accelerometer data
        accelerometer_obj.display();
        // Write the accelerometer data if the recording has started
        if (MainActivity.is_recording) {
            accelerometer_obj.write();
        }

        // Check if the thread has been interrupted
        if (this.isInterrupted()) {
            // Request cancellation for updates on accelerometer data
            sensor_manager.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
