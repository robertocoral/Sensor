package com.edu.slu.sensor;

import android.os.Message;

/**
 * This class represents attributes and methods pertaining to the gyroscope
 * Created by sashank on 12/2/15.
 */
public class GyroscopeObject {
    private long time = 0;
    private int accuracy = 0;
    private float x_axis = 0;
    private float y_axis = 0;
    private float z_axis = 0;

    /**
     * Default constructor
     * @param system_time
     * @param accuracy
     * @param x_axis
     * @param y_axis
     * @param z_axis
     */
    public GyroscopeObject(long system_time, int accuracy, float x_axis, float y_axis, float z_axis) {
        this.time = system_time - MainActivity.start_time;
        this.accuracy = accuracy;
        this.x_axis = x_axis;
        this.y_axis = y_axis;
        this.z_axis = z_axis;
    }

    /**
     * Write the gyroscope data to a file
     */
    public void write() {
        // If this is a new file, write header to file
        if(!FileUtils.exists(MainActivity.gyro_file)) {
            String header = "System Time,Sensor,Accuracy,X Axis,Y Axis,Z Axis\n";
            FileUtils.write(MainActivity.gyro_file, header);
        }

        // Create a string line with sensor data and write the data to file
        String line = String.format("%d,Gyroscope,%d,%f,%f,%f\n", this.time, this.accuracy, this.x_axis, this.y_axis, this.z_axis);
        FileUtils.write(MainActivity.gyro_file, line);
    }

    /**
     * Display the gyroscope data on the screen
     */
    public void display() {
        // Compose the message object and create a string line with sensor data
        Message message = Message.obtain();
        message.obj = String.format("Gyroscope -> X: %.02f Y: %.02f Z: %.02f", this.x_axis, this.y_axis, this.z_axis);

        // Send the message to the message handler
        UIUtils.gyro_handler.sendMessage(message);
    }
}
