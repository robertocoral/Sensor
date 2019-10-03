package com.edu.slu.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Thread that registers, unregisters the Magnetometer and processes the Magnetometer data
 * Created by sashank on 1/22/15.
 */
public class MagnetometerThread extends Thread implements SensorEventListener {
    private Context context = null;
    private float[] mag_values = null, grav_values = null;
    private SensorManager sensor_manager = null;

    /**
     * Default constructor
     * @param context
     */
    public MagnetometerThread(Context context) {
        this.context = context;
    }

    /**
     * Register the magnetometer and the gravity sensor
     */
    @Override
    public void run() {
        super.run();

        // Obtain an instance of the sensor service and register both the magnetometer and gravity sensor
        sensor_manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor_manager.registerListener(this, sensor_manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 100000);
        sensor_manager.registerListener(this, sensor_manager.getDefaultSensor(Sensor.TYPE_GRAVITY), 100000);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] rotation_matrix = new float[9];
        float[] orientation = new float[3];

        // If the sensor type is magnetometer
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mag_values = event.values.clone();
        }
        // If the sensor type is gravity
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            grav_values = event.values.clone();
        }

        // Ensure that this is a magnetometer event and both magnetometer and gravity values are set
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD && (mag_values != null && grav_values != null)) {
            // Obtain the rotation matrix and the orientation from the rotation matrix
            SensorManager.getRotationMatrix(rotation_matrix, null, grav_values, mag_values);
            SensorManager.getOrientation(rotation_matrix, orientation);

            // Calculate the magnetometer strength and heading in degrees
            double strength = Math.sqrt(Math.pow(mag_values[0], 2) + Math.pow(mag_values[1], 2) + Math.pow(mag_values[2], 2));
            double heading = (Math.toDegrees(orientation[0]) + 360) % 360;

            MagnetometerObject magnetometer_obj = new MagnetometerObject(System.currentTimeMillis(), event.accuracy, event.values[2], strength, heading);
            // Display the magnetometer data
            magnetometer_obj.display();
            // Write the magnetometer data if the recording has started
            if (MainActivity.is_recording) {
                magnetometer_obj.write();
            }
        }

        // Check if the thread has been interrupted
        if (this.isInterrupted()) {
            // Request cancellation for updates to gravity and magnetometer data
            sensor_manager.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}