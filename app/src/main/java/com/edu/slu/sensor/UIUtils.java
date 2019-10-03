package com.edu.slu.sensor;

import android.os.Handler;
import android.os.Message;

/**
 * This class represents all the attributes and methods pertaining to ui specific operations
 * Created by sashank on 12/2/15.
 */
public class UIUtils {
    /**
     * Set handler to send message to accelerometer text box
     */
    public static Handler acc_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            MainActivity.acc_text.setText((String) msg.obj);
        }
    };

    /**
     * Set handler to send message to gyroscope text box
     */
    public static Handler gyro_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            MainActivity.gyro_text.setText((String) msg.obj);
        }
    };

    /**
     * Set handler to send message to magnetometer text box
     */
    public static Handler mag_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            MainActivity.mag_text.setText((String) msg.obj);
        }
    };

    /**
     * Set handler to send message to status text box
     */
    public static Handler status_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            MainActivity.status_text.append(msg.obj + "\n");
        }
    };

    /**
     * Wrapper to send a string line to the status handler
     * @param line
     */
    public static void sendMessage(String line) {
        // Compose the message object and set line
        Message message = Message.obtain();
        message.obj = line;

        // Send the message to the message handler
        //status_handler.sendMessage(message);
    }
}
