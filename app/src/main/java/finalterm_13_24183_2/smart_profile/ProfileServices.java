package finalterm_13_24183_2.smart_profile;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.widget.Toast;



public class ProfileServices extends Service implements SensorEventListener {

    public AudioManager audioManager;
    public SensorManager sensorManager;
    public Sensor accelerometerSensor,proximitySensor,lightSensor;


    //Shaking Part Variables
    public long lastUpdate = 0;
    public float last_x,last_y,last_z;
    public static final int SHAKE_THRESHOLD = 700;

    //For Condition Flag
    public boolean _faceUp=true,_inFront=true,_lightOn=true,_shacking=true;

    @Override
    public void onCreate() {
        super.onCreate();
        //getting audio services
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        //Getting Sensor Service
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //Now Getting required Sensors
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        //Now Check Sensor Availability
        if (accelerometerSensor != null){
            sensorManager.registerListener(this,accelerometerSensor,SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (proximitySensor != null){
            sensorManager.registerListener(this,proximitySensor,SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (lightSensor != null){
            sensorManager.registerListener(this,lightSensor,SensorManager.SENSOR_DELAY_NORMAL);
        }
    }



    //When Service Start
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this,"Service Enable",Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    //When Service Destroyed
    //Sensor Unregister
    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this,"Service Disable",Toast.LENGTH_SHORT).show();
        sensorManager.unregisterListener(this);
    }

    //For Binding The service to mainActivity
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    //Where Sensor Works
    @Override
    public void onSensorChanged(SensorEvent event) {

        //For Light
        if (event.sensor.getType() == Sensor.TYPE_LIGHT){
            if (event.values[0] < 6){
                _lightOn = false;
            } else if (event.values[0] >= 10){
                _lightOn = true;
            }
        }

        //For Proximity
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY){
            if (event.values[0] == 0){
                _inFront = true;
            } else {
                _inFront = false;
            }
        }


        //For Accelerometer
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            //For Phone Face Up And Face Down

            if ((x < 2.0 && x > -2.0) && (y < 2.0 && y> -2.0) && z > 8.0){
                //Face Up
                _faceUp = true;
                //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            } else if (z < 1){
                _faceUp = false;
                //audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }

            // For Phone Shaking Part

            //Stores System Time
            long currentTime = System.currentTimeMillis();

            //Checks After 100 milli second for battery performance
            if ((currentTime - lastUpdate) > 100){
                long differentTime = (currentTime - lastUpdate);
                lastUpdate = currentTime;

                //Now Calculating the speed of shaking
                float speed = Math.abs(x + y+ z - last_x - last_y - last_z)/differentTime*10000;

                if (speed > SHAKE_THRESHOLD){
                    //Device Shake
                    //Toast.makeText(this,"Shaking",Toast.LENGTH_SHORT).show();
                    _shacking = true;
                } else {
                    _shacking = false;
                }
            }
        }

        //Profile Manager

        if (_faceUp && !_inFront && _lightOn){
            // For Home Profile
            //No Vibration, Ringer Loud
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audioManager.setStreamVolume(AudioManager.STREAM_RING,audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),0);
            SystemClock.sleep(20);

        } else if (_shacking && _inFront && !_lightOn){
            //Pocket Profile
            //Vibration On, Ringer Medium
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audioManager.setStreamVolume(AudioManager.STREAM_RING,20,0);
            SystemClock.sleep(20);

        } else if (!_faceUp && _inFront && !_lightOn){
            //Silent Profile
            //Only Vibration
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            SystemClock.sleep(20);
        }

    }


    //Not In Use For Now
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

}
