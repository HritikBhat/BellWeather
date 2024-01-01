package com.hritikbhat.bellweather.util.services

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.hritikbhat.bellweather.data.enums.TriggerSource
import com.hritikbhat.bellweather.data.db.tables.AppTable
import com.hritikbhat.bellweather.ui.activities.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class NotificationService : NotificationListenerService() {

    private var activeHandledNotificationKeys: MutableSet<String> = mutableSetOf()
    private var lastMessageTime: Long = 0

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)


        if (activeHandledNotificationKeys.contains(sbn.key) && Calendar.getInstance().timeInMillis - lastMessageTime < 800) return

        /* Handle unique StatusBarNotification */

        activeHandledNotificationKeys.add(sbn.key)
        lastMessageTime = Calendar.getInstance().timeInMillis
        val context = this

        // Check if the notification is from a specific package
        if (MainActivity.isServiceEnabled){

            val notificationContent = sbn.notification.extras.getCharSequence(Notification.EXTRA_TEXT).toString()

            MainActivity.appAlertService?.coroutineScopeMain?.launch(Dispatchers.Main) {
                var appTable: AppTable?=null
                MainActivity.appAlertService?.coroutineScopeIO?.launch(Dispatchers.IO) {
                    appTable = checkIfAppFiltersTriggered(context,notificationContent,
                        TriggerSource.NOTIFICATIONS
                    )
                }?.join()

                if (appTable!=null){
                    sbn.notification.extras.getCharSequence(Notification.EXTRA_TITLE).toString()

                    MainActivity.appAlertService?.ringAlert(context,appTable!!,notificationContent,
                        TriggerSource.NOTIFICATIONS
                    )
                }
                delay(1000)
            }

        }

    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        activeHandledNotificationKeys.remove(sbn.key)
    }



}

