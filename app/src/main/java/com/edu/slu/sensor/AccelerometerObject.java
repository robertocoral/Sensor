package com.edu.slu.sensor;

import android.os.Message;

/**
 * This class represents attributes and methods pertaining to the Accelerometer
 * Created by sashank on 12/2/15.
 */
public class AccelerometerObject {
    private long time = 0;
    private int accuracy = 0;
    private float x_axis = 0;
    private float y_axis = 0;
    private float z_axis = 0;
    private double magnitude = 0.;

    /**
     * Default constructor
     * @param system_time
     * @param accuracy
     * @param x_axis
     * @param y_axis
     * @param z_axis
     */
    public AccelerometerObject(long system_time, int accuracy, float x_axis, float y_axis, float z_axis) {
        this.time = system_time - MainActivity.start_time;
        this.accuracy = accuracy;
        this.x_axis = x_axis;
        this.y_axis = y_axis;
        this.z_axis = z_axis;

        // Calculate the magnitude
        this.magnitude = Math.sqrt(Math.pow(this.x_axis, 2) + Math.pow(this.y_axis, 2) + Math.pow(this.z_axis, 2));
    }

    /**
     * Write the accelerometer data to a file
     */
    public void write() {
        // If this is a new file, write header to file
        if(!FileUtils.exists(MainActivity.acc_file)) {
            String header = "System Time,Sensor,Accuracy,X Axis,Y Axis,Z Axis,Magnitude\n";
            FileUtils.write(MainActivity.acc_file, header);
        }

        // Create a string line with sensor data and write the data to file
        String line = String.format("%d,Accelerometer,%d,%f,%f,%f,%f\n", this.time, this.accuracy, this.x_axis, this.y_axis, this.z_axis, this.magnitude);
        FileUtils.write(MainActivity.acc_file, line);
    }

    /**
     * Display the accelerometer data on the screen
     */
    public void display() {
        // Compose the message object and create a string line with sensor data
        Message message = Message.obtain();
        message.obj = String.format("Accelerometer -> X: %.02f Y: %.02f Z: %.02f M: %.02f", this.x_axis, this.y_axis, this.z_axis, this.magnitude);

        // Send the message to the message handler
        UIUtils.acc_handler.sendMessage(message);
    }
}
