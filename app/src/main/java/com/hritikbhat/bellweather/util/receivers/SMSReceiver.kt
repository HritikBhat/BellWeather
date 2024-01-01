package com.hritikbhat.bellweather.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.widget.Toast
import com.hritikbhat.bellweather.data.db.tables.AppTable
import com.hritikbhat.bellweather.data.enums.TriggerSource
import com.hritikbhat.bellweather.ui.activities.MainActivity
import com.hritikbhat.bellweather.util.services.checkIfAppFiltersTriggered
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SMSReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {

            if (MainActivity.isServiceEnabled){
                    val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                    val smsBody = StringBuilder()
                    for (sms in messages) {
                        smsBody.append(sms.displayMessageBody)
                    }

                MainActivity.appAlertService?.coroutineScopeMain?.launch(Dispatchers.Main) {
                        var appTable: AppTable?=null
                    MainActivity.appAlertService?.coroutineScopeIO?.launch(Dispatchers.IO) {
                            appTable = checkIfAppFiltersTriggered(context,smsBody.toString(),
                                TriggerSource.SMS
                            )
                        }?.join()

                        if (appTable!=null){
                            MainActivity.appAlertService?.ringAlert(
                                context,
                                appTable!!,
                                "$smsBody",
                                TriggerSource.SMS
                            )
                            }
                    }
                }

        } else if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
            // Handle boot completed event if needed
            Toast.makeText(context, "Boot Completed", Toast.LENGTH_SHORT).show()
        }
    }
}

