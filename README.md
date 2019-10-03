# Sensor

This Android app records the accelerometer, gyroscope, and magnetometer data and stores them in a Firebase Cloud Storage.
To make the app work with Firebase, you need to:
1) sign in on Firebase using your Google account: https://firebase.google.com/
2) create a new Firebase Project (from the Firebase Console, in your personal page)
3) add Firebase to the app, following the steps 1, 2, 3.a of this tutorial: https://firebase.google.com/docs/android/setup
4) create a default Storage bucket and set up public access, following the sections "Create a default Storage bucket" 
   and "Set up public access" of this tutorial: https://firebase.google.com/docs/storage/android/start
   (the root of the Storage bucket has to be a folder called "sensors")




This app is an adaptation of a previous app developed by Sashank Narain.
