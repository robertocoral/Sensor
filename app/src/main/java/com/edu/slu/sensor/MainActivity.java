package com.edu.slu.sensor;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Date;

import static android.content.ContentValues.TAG;


public class MainActivity extends Activity {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 2;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_ = 3;
    private static final int ALL_PERMISSIONS = 100;

    private boolean mLocationPermissionGranted;
    private boolean mStoragePermissionGranted;

    private Button startButton;
    private Button stopButton;

    private Thread acc_thread = null, gyro_thread = null, mag_thread = null;

    private String date;

    private boolean isGPSOn;
    private boolean recording;

    public static boolean is_recording = false;
    public static long start_time = 0;
    public static String acc_file = null, gyro_file = null, mag_file = null, start_file = null, end_file = null;
    public static TextView acc_text = null, gyro_text = null, mag_text = null, status_text = null;

    private StorageReference storageRef;
    private UploadTask uploadTask;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Disable screen lock and timeout while activity is in foreground
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initialize all the controls present in the activity
        acc_text = (TextView) findViewById(R.id.txtAccelerometer);
        gyro_text = (TextView) findViewById(R.id.txtGyroscope);
        mag_text = (TextView) findViewById(R.id.txtMagnetometer);
        status_text = (TextView) findViewById(R.id.txtRecordStatus);

        startButton = (Button)findViewById(R.id.startRecord);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });

        stopButton = (Button)findViewById(R.id.stopRecord);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });

        // Set scrolling for textview
        status_text.setMovementMethod(new ScrollingMovementMethod());

        FirebaseStorage mStorage = FirebaseStorage.getInstance();

        mLocationPermissionGranted = false;
        mStoragePermissionGranted = false;
        recording = false;

        getPermissions();

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isGPSOn = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // Create a storage reference from our app
        storageRef = mStorage.getReference();

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Start all the location and sensor threads
        startThreads();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop all the location and sensor threads
        stopThreads();
    }


    private void startRecording() {
        if (!is_recording) {
            // Set the recording flag
            is_recording = true;
            recording = true;
            Toast.makeText(getApplicationContext(), "Start Recording!!!", Toast.LENGTH_SHORT).show();
            date = new Date().toString().replace(" ", "_");
            if (isGPSOn) {
                Log.d(TAG, "GPS ON");
                getDeviceLocation("start");
            }
            // Set the file names
            setFileNames();
            // Set the recording start time
            start_time = System.currentTimeMillis();

            UIUtils.sendMessage("Recording Started.");
        }
    }

    private void stopRecording() {
        if (is_recording) {
            // Set the recording flag
            is_recording = false;
            recording = false;
            Toast.makeText(getApplicationContext(), "Stop Recording!!!", Toast.LENGTH_SHORT).show();
            if (isGPSOn) {
                getDeviceLocation("end");
            }

            UIUtils.sendMessage("Recording Stopped.");
            uploadFilesOnFirebase();
        }
    }

    /**
     * Set the directory name and all the file names for this recording
     */
    private void setFileNames() {
        // Set the model name and id of the phone
        String phone_id = Build.MODEL.replace(" ", "") + "_" + Settings.System.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Setup the directory name and create the directory
        String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Research/SensorsDataCollection/" + "/" + phone_id.toUpperCase() + "/" + date + "/";
        FileUtils.createDirectory(directory);

        // Setup the file names for the recording files
        acc_file = directory + "Accelerometer.csv";
        gyro_file = directory + "Gyroscope.csv";
        mag_file = directory + "Magnetometer.csv";
        start_file = directory + "StartingPosition.csv";
        end_file = directory + "EndingPosition.csv";
    }

    /**
     * Start the location and sensor threads
     */
    private void startThreads() {
        // Initialize the  accelerometer, gyroscope and magnetometer threads
        acc_thread = new AccelerometerThread(getApplicationContext());
        gyro_thread = new GyroscopeThread(getApplicationContext());
        mag_thread = new MagnetometerThread(getApplicationContext());

        // Start the location, accelerometer, gyroscope and magnetometer threads
        acc_thread.start();
        gyro_thread.start();
        mag_thread.start();
    }

    /**
     * Stop the location and sensor threads
     */
    private void stopThreads() {
        // Stop the accelerometer, gyroscope and magnetometer threads
        acc_thread.interrupt();
        gyro_thread.interrupt();
        mag_thread.interrupt();
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation(final String status) {
        Log.d(TAG, "getDeviceLocation");
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
                            storeLocation(mLastKnownLocation, status);
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void uploadFilesOnFirebase() {
        String phone_id = Build.MODEL.replace(" ", "") + "_" + Settings.System.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Uri accelerometerFile = Uri.fromFile(new File(acc_file));
        Uri gyroscopeFile = Uri.fromFile(new File(gyro_file));
        Uri magnetometerFile = Uri.fromFile(new File(mag_file));
        StorageReference accelerometerRef = storageRef.child("sensors/" + phone_id.toUpperCase() + "/" + date + "/" + accelerometerFile.getLastPathSegment());
        StorageReference gyroscopeRef = storageRef.child("sensors/" + phone_id.toUpperCase() + "/" + date + "/" + gyroscopeFile.getLastPathSegment());
        StorageReference magnetometerRef = storageRef.child("sensors/" + phone_id.toUpperCase() + "/" + date + "/" + magnetometerFile.getLastPathSegment());
        uploadTask = accelerometerRef.putFile(accelerometerFile);
        uploadTask = gyroscopeRef.putFile(gyroscopeFile);
        uploadTask = magnetometerRef.putFile(magnetometerFile);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                System.out.println("Upload on Firebase Storage failed: " + exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                System.out.println("Upload on Firebase Storage succesfully done" );
            }
        });
    }

    private void storeLocation(Location location, String status) {
        String phone_id = Build.MODEL.replace(" ", "") + "_" + Settings.System.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Log.d(TAG, "storeLocation");
        Uri startingLocationFile = Uri.fromFile(new File(start_file));
        Uri endingLocationFile = Uri.fromFile(new File(end_file));
        StorageReference startingLocationRef = storageRef.child("sensors/" + phone_id.toUpperCase() + "/" + date + "/" + startingLocationFile.getLastPathSegment());
        StorageReference endingLocationRef = storageRef.child("sensors/" + phone_id.toUpperCase() + "/" + date + "/" + endingLocationFile.getLastPathSegment());
        if (status.equals("start")) {
            // If this is a new file, write header to file
            if(!FileUtils.exists(MainActivity.start_file)) {
                String header = "System Time,Latitude,Longitude\n";
                FileUtils.write(start_file, header);
            }

            // Create a string line with location data and write the data to file
            String line = String.format("%d,%f,%f\n", System.currentTimeMillis() - MainActivity.start_time, location.getLatitude(), location.getLongitude());
            FileUtils.write(MainActivity.start_file, line);

            uploadTask = startingLocationRef.putFile(startingLocationFile);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.e(TAG, "Upload on Firebase Storage failed (Starting Position): " + exception.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    System.out.println("Upload on Firebase Storage succesfully done (Starting Position)" );
                }
            });
        }
        else if (status.equals("end")) {
            // If this is a new file, write header to file
            if(!FileUtils.exists(MainActivity.end_file)) {
                String header = "System Time,Latitude,Longitude\n";
                FileUtils.write(end_file, header);
            }

            // Create a string line with location data and write the data to file
            String line = String.format("%d,%f,%f\n", System.currentTimeMillis() - MainActivity.start_time, location.getLatitude(), location.getLongitude());
            FileUtils.write(MainActivity.end_file, line);

            uploadTask = endingLocationRef.putFile(endingLocationFile);
            uploadTask = endingLocationRef.putFile(endingLocationFile);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.e(TAG,"Upload on Firebase Storage failed (Ending Position): " + exception.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    System.out.println("Upload on Firebase Storage succesfully done (Ending Position)" );
                }
            });
        }
    }


    private void getPermissions() {
        final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if ((ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)) {
            mStoragePermissionGranted = true;
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    ALL_PERMISSIONS);
        }
    }

    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                mStoragePermissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void getStoragePermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            mStoragePermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case ALL_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                }
                else {
                    getPermissions();
                }
            }
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getStoragePermission();
                }
                else {
                    getLocationPermission();
                }
            }

            case PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }

            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                else {
                    getStoragePermission();
                }
            }
        }
    }
}