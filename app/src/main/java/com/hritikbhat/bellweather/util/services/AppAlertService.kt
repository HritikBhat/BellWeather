package com.hritikbhat.bellweather.util.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.Telephony
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.hritikbhat.bellweather.R
import com.hritikbhat.bellweather.util.receivers.SMSReceiver
import com.hritikbhat.bellweather.data.enums.TriggerSource
import com.hritikbhat.bellweather.data.db.AppDatabase
import com.hritikbhat.bellweather.data.db.tables.AppSettingTable
import com.hritikbhat.bellweather.data.db.tables.AppTable
import com.hritikbhat.bellweather.ui.activities.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AppAlertService : Service()  {
    private var myBinder = MyBinder()
    private val channelId = "ForegroundServiceChannel"
    private val notificationId = 101
    var mediaPlayer: MediaPlayer? = null
    var smsReceiver: SMSReceiver = SMSReceiver()
    var isAppAlertServiceEnabled:Boolean = false

    var coroutineScopeMain: CoroutineScope? = null
    var coroutineScopeIO: CoroutineScope? = null
    var textToSpeech: TextToSpeech?= null

    inner class MyBinder: Binder() {
        fun currentService(): AppAlertService {
            return this@AppAlertService
        }
    }

    private fun registerReceivers() {
        val smsIntentFilter = IntentFilter().apply {
            addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        }
        registerReceiver(smsReceiver, smsIntentFilter)
    }

    fun startService(){
        textToSpeech = TextToSpeech( this) {
            registerReceivers()
        }
        mediaPlayer = MediaPlayer()
        isAppAlertServiceEnabled=true
        coroutineScopeMain = CoroutineScope(Dispatchers.Main)
        coroutineScopeIO = CoroutineScope(Dispatchers.IO)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startForeground(notificationId, createNotification())
            Log.d("Started Service","Enabled")
        } else {
            //For API 34 and above
            startForeground(notificationId, createNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        }
    }


    override fun onCreate() {
        super.onCreate()
        startService()
    }



    override fun onBind(intent: Intent): IBinder {
        return myBinder
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)

        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            flag
        )


        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("BellWeather")
            .setContentText("Active: To Close, Tap To Open App")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        return notification
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channelName = "Foreground Service Channel"
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun ringAlert(
        context: Context,
        appTable: AppTable,
        smsBody: String,
        notifications: TriggerSource,
    ){
        coroutineScopeIO?.launch {
            var appSettingTable:AppSettingTable?=null
            coroutineScopeIO?.launch(Dispatchers.IO){
                appSettingTable = AppDatabase(context).getAppDao().getAppSettingByAID(appTable.aId)

            }?.join()
            if (appSettingTable?.ringORSpeech==1){
                withContext(Dispatchers.Main){

                    delay(1500)
                    textToSpeech?.speak(smsBody,TextToSpeech.QUEUE_FLUSH,null,null)
                }
            }
            else{
                withContext(Dispatchers.Main){
//                    Toast.makeText(context, "Ring Received From ${appTable.aName}: $smsBody", Toast.LENGTH_SHORT).show()
                    //Add MediaPlayer Ring Logic
                    val afd = context.resources.openRawResourceFd(R.raw.my_audio)
                    mediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    mediaPlayer?.prepare()
                    mediaPlayer?.start()
                }
            }
        }

        }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(smsReceiver)
        coroutineScopeMain = null
        coroutineScopeIO = null
        textToSpeech = null
        mediaPlayer = null
    }
}
