package com.hritikbhat.bellweather.util.services

import android.content.Context
import android.util.Log
import com.hritikbhat.bellweather.data.enums.TriggerSource
import com.hritikbhat.bellweather.data.db.AppDatabase
import com.hritikbhat.bellweather.data.db.tables.AppSettingTable
import com.hritikbhat.bellweather.data.db.tables.AppTable
import com.hritikbhat.bellweather.ui.activities.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private fun isGivenTriggerSource(notifications: TriggerSource, appSettingTable: AppSettingTable):Boolean{
    val flag = when(notifications){
        TriggerSource.NOTIFICATIONS -> {
            appSettingTable.isNotificationEnabled==1
        }
        TriggerSource.SMS ->{
            appSettingTable.isSMSEnabled==1
        }
    }
    return flag
}

suspend fun checkIfAppFiltersTriggered(context: Context, searchString: String, notifications: TriggerSource): AppTable? {
    return suspendCoroutine { continuation ->
        MainActivity.appAlertService?.coroutineScopeIO?.launch(Dispatchers.IO) {
            try {
                val keywordArraylist = AppDatabase(context).getAppDao().getAllAppKeywordsBySearchString(searchString)
                Log.d("Trigger KeywordArr", keywordArraylist.toString())

                if (keywordArraylist.isNotEmpty()) {
                    val functionFlag = keywordArraylist.any { keyword ->
                        val appSettingTable = AppDatabase(context).getAppDao().getAppSettingByAID(keyword.aId)
                        searchString.contains(keyword.kName) && isGivenTriggerSource(notifications, appSettingTable)
                    }
                    if (functionFlag) {
                        val app = keywordArraylist.firstOrNull {
                            searchString.contains(it.kName)
                        }?.let { AppDatabase(context).getAppDao().getApp(it.aId) }

                        withContext(Dispatchers.Main) {
                            continuation.resume(app)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            continuation.resume(null)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        continuation.resume(null)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    continuation.resumeWithException(e)
                }
            }
        }
    }
}

