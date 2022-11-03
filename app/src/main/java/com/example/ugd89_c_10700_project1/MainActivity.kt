package com.example.ugd89_c_10700_project1

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.hardware.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity(), SensorEventListener {

    lateinit var proximitySensor: Sensor
    lateinit var sensorManager: SensorManager
    private var mCamera: Camera? = null
    private var mCameraView: CameraView? = null
    private var mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK
    private val CHANNEL_ID = "channel_notification"
    private val noticationId = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        setUpSensorStuff()
        createNotificationChanel()


        try{
            mCamera = Camera.open(0)
        }catch (e: java.lang.Exception){
            Log.d("Error", "Failed to get Camera" + e.message)
        }
        if(mCamera !=null){
            mCameraView = CameraView(this, mCamera!!)
            val  camera_view = findViewById<View>(R.id.FLCamera) as FrameLayout
            camera_view.addView(mCameraView)
        }
        
        if (proximitySensor == null) {
            Toast.makeText(this, "No proximity sensor found in device..", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            sensorManager.registerListener(
                proximitySensorEventListener,
                proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        @SuppressLint("MissingInflatedId", "LocalSuppress") val imageClose =
            findViewById<View>(R.id.imgClose) as ImageButton
        imageClose.setOnClickListener{view: View? -> System.exit(0)}
    }

    var proximitySensorEventListener: SensorEventListener? = object: SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            //
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
                if (event.values[0] == 0f) {
                    if (mCameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    } else {
                        mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
                    }
                    if (mCameraView != null) {
                        mCamera?.stopPreview();
                    }
                    mCamera?.release();
                    try {
                        mCamera = Camera.open(mCameraID)
                    } catch (e: Exception) {
                        Log.d("Error", "Failed to get Camera" + e.message)
                    }
                    if (mCamera != null) {
                        mCameraView = CameraView(applicationContext, mCamera!!)
                        val camera_view = findViewById<View>(R.id.FLCamera) as FrameLayout
                        camera_view.addView(mCameraView)

                    }
                }
            }
        }
    }

    private fun setUpSensorStuff(){
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        // Specify the sensor you want to listen to
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also{ accelerometer ->
            sensorManager.registerListener(this,
                accelerometer,
                SensorManager.SENSOR_DELAY_FASTEST,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Checks for the sensor we have registered
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            //Log.d("Main", "onSensorChanged: sides ${event.values[0]} front/back ${event.values[1]} ")
            // Sides = Tilting phone left(10) and right(-10)
            val sides = event.values[0]
            // Up/Down = Tilting phone up(10), flat (0), upside-down(- 10)x
            val upDown = event.values[1]

            if(sides.toInt() > 5 || upDown.toInt() > 5 || sides.toInt() < -5 || upDown.toInt() < -5 || sides.toInt() == 0 && upDown.toInt() ==0){
                sendNotification()
            }

        }
    }
//    notifikasi
    private fun createNotificationChanel(){
        val name = "Notification Title"
        val descriptionText = "Notifivation Description"

        val channel1 = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel1)

    }

    private fun sendNotification(){
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_mail)
            .setContentTitle("Modul89_C_10700_PROJECT2")
            .setContentText("Selamat anda sudah berhasil mengerjakan Modul 8 dan 9")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setColor(Color.RED)

        with(NotificationManagerCompat.from(this)){
            notify(noticationId, builder.build())
        }
    }
}