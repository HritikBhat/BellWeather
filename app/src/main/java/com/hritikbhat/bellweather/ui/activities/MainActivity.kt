package com.hritikbhat.bellweather.ui.activities

//import android.os.Build
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Context
//import android.graphics.Color
//import androidx.core.app.NotificationCompat

import android.Manifest
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.hritikbhat.bellweather.util.services.AppAlertService
import com.hritikbhat.bellweather.R
import com.hritikbhat.bellweather.util.receivers.SMSReceiver
import com.hritikbhat.bellweather.data.sharedPref.SharedPreferenceInstance
import com.hritikbhat.bellweather.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), ServiceConnection {


    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var notificationSettingsLauncher: ActivityResultLauncher<Intent>
    private lateinit var sharedPref: SharedPreferences

    companion object{
        var appAlertService : AppAlertService? = null
        var isServiceEnabled:Boolean = false
    }


    override fun onResume() {
        super.onResume()
        if (appAlertService!=null){
            if (appAlertService?.mediaPlayer!!.isPlaying){
                appAlertService?.mediaPlayer!!.stop()
                appAlertService?.mediaPlayer!!.reset()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        navController = Navigation.findNavController(this, R.id.navHostFragment)


        sharedPref = SharedPreferenceInstance(this).getSPInstance()

        //When service in bg and user clicks Notification to come back to UI
        isServiceEnabled = if (appAlertService!=null){
            sharedPref.getBoolean("serviceEnabled",false) && appAlertService!!.isAppAlertServiceEnabled
        } else{
            false
        }

        binding.appServiceButton.isChecked = isServiceEnabled


        //set the appbar
        setSupportActionBar(binding.topAppBar)

        NavigationUI.setupActionBarWithNavController(this,navController)

        notificationSettingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (!isNotificationServiceEnabled()) {
                Toast.makeText(this, "Please enable the notification listener to this app or else it will not work", Toast.LENGTH_LONG).show()
            }
        }

        requestPermissions()

        startNotificationListenerService()


        binding.appServiceButton.setOnClickListener {
            isServiceEnabled =binding.appServiceButton.isChecked
            sharedPref.edit().putBoolean("serviceEnabled",isServiceEnabled).apply()
            if (isServiceEnabled){
                Log.d("Service Status:::","Enabled")
                val intent2 = Intent(this, AppAlertService::class.java)
                bindService(intent2, this, BIND_AUTO_CREATE)
                startService(intent2)
                Toast.makeText(this,"Service Enabled",Toast.LENGTH_SHORT).show()
                appAlertService?.startService()
            }
            else{
                Log.d("Service Status:::","Disabled")
                appAlertService!!.stopForeground(Service.STOP_FOREGROUND_REMOVE)
                Toast.makeText(this,"Service Disabled",Toast.LENGTH_SHORT).show()
            }

        }

    }

    //This method will get trigger when user clicks back in second fragment
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(
            navController,
            null)
    }



private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
        ) } else {
        arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.POST_NOTIFICATIONS
        ) }

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, permissions, 1)
            }
        }
    }

    private fun startNotificationListenerService() {
        if (!isNotificationServiceEnabled()) {
            // Request the user to grant permission to use Notification Listener
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            notificationSettingsLauncher.launch(intent)
            Toast.makeText(this, "Please enable the notification listener to this app and then press back", Toast.LENGTH_LONG).show()
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val packageName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(packageName)
    }

//    private fun showNotification(context: Context, title: String, message: String) {
//        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        // Create a unique notification channel ID for Android Oreo and higher
//        val channelId = "your_channel_id"
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(channelId, "Your Channel Name", NotificationManager.IMPORTANCE_DEFAULT)
//            channel.description = "Your Channel Description"
//            channel.enableLights(true)
//            channel.lightColor = Color.BLUE
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        // Build the notification
//        val notificationBuilder = NotificationCompat.Builder(context, channelId)
//            .setSmallIcon(R.drawable.ic_launcher_foreground) // Set your notification icon
//            .setContentTitle(title)
//            .setContentText(message)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//
//        // Display the notification
//        notificationManager.notify(123, notificationBuilder.build())
//    }

    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
        val binder = service as AppAlertService.MyBinder
        appAlertService = binder.currentService()
        appAlertService?.smsReceiver = SMSReceiver()

    }

    override fun onServiceDisconnected(name: ComponentName?) {
        appAlertService = null
    }

}

