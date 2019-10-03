package com.edu.slu.sensor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * This class represents all the attributes and methods pertaining to file specific operations
 * Created by sashank on 12/2/15.
 */
public class FileUtils {
    /**
     * Create a directory with name
     * @param dir_name
     */
    public static void createDirectory(String dir_name) {
        // Create file object and the directory
        File dir = new File(dir_name);
        dir.mkdirs();
    }

    /**
     * Check if file exists
     * @param file_name
     */
    public static boolean exists(String file_name) {
        // Create file object
        File file = new File(file_name);
        // Return whether file exists or not
        return file.exists();
    }

    /**
     * Write the line passed as an argument to the file
     * @param file_name
     * @param text
     */
    public static void write(String file_name, String text) {
        try {
            // Open a writer to the file and write the line to the file
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file_name, true));
            writer.write(text);
            writer.close();
        } catch (Exception e) {
            UIUtils.sendMessage("Error writing to file. Error: " + e.getMessage());
        }
    }
}
