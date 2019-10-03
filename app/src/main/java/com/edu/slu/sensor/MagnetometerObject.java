package com.edu.slu.sensor;

import android.os.Message;

/**
 * This class represents attributes and methods pertaining to the magnetometer
 * Created by sashank on 12/2/15.
 */
public class MagnetometerObject {
    private long time = 0;
    private int accuracy = 0;
    private float z_axis = 0;
    private double strength = 0.;
    private double heading = 0.;

    /**
     * Default constructor
     * @param system_time
     * @param accuracy
     * @param z_axis
     * @param strength
     * @param heading
     */
    public MagnetometerObject(long system_time, int accuracy, float z_axis, double strength, double heading) {
        this.time = system_time - MainActivity.start_time;
        this.accuracy = accuracy;
        this.z_axis = z_axis;
        this.strength = strength;
        this.heading = heading;
    }

    /**
     * Write the magnetometer data to a file
     */
    public void write() {
        // If this is a new file, write header to file
        if(!FileUtils.exists(MainActivity.mag_file)) {
            String header = "System Time,Sensor,Accuracy,Z Axis,Strength,Heading\n";
            FileUtils.write(MainActivity.mag_file, header);
        }

        // Create a string line with sensor data and write the data to file
        String line = String.format("%d,Magnetometer,%d,%f,%f,%f\n", this.time, this.accuracy, this.z_axis, this.strength, this.heading);
        FileUtils.write(MainActivity.mag_file, line);
    }

    /**
     * Display the magnetometer data on the screen
     */
    public void display() {
        // Compose the message object and create a string line with sensor data
        Message message = Message.obtain();
        message.obj = String.format("Magnetometer -> Z: %.02f S: %.02f H: %.02f", this.z_axis, this.strength, this.heading);

        // Send the message to the message handler
        UIUtils.mag_handler.sendMessage(message);
    }
}
