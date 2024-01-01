package com.hritikbhat.bellweather.data.viewmodel

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import com.hritikbhat.bellweather.data.db.AppDao
import com.hritikbhat.bellweather.data.db.tables.AppSettingTable
import com.hritikbhat.bellweather.data.db.tables.AppTable
import com.hritikbhat.bellweather.data.db.tables.KeywordTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class MainViewModel(private val appDao: AppDao) : ViewModel() {

    fun getAllApps(): Flow<List<AppTable>> = flow {
        emit(appDao.getAllApps())
    }.flowOn(Dispatchers.IO)

    fun getAllAppKeywords(aId:Int): Flow<List<KeywordTable>> = flow {
        emit(appDao.getAllAppKeywords(aId))
    }.flowOn(Dispatchers.IO)

    fun getAppSetting(aId:Int): Flow<AppSettingTable> = flow {
        emit(appDao.getAppSetting(aId))
    }.flowOn(Dispatchers.IO)

    suspend fun updateAppSetting(appSettingTable: AppSettingTable){
        withContext(Dispatchers.IO){
            appDao.updateAppSetting(appSettingTable)
        }
    }


    suspend fun getAllInstalledApps(packageManager:PackageManager): Flow<List<AppTable>> = withContext(Dispatchers.IO) {
        flow {
            val appsList = ArrayList<AppTable>()

            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val resolveInfos: List<ResolveInfo> =
                packageManager.queryIntentActivities(mainIntent, 0)

            for (resolveInfo in resolveInfos) {
                val appName = resolveInfo.loadLabel(packageManager).toString()
                val packageName = resolveInfo.activityInfo.packageName
                val appIcon:Bitmap = resolveInfo.loadIcon(packageManager).toBitmap()

                val appDetail = AppTable(appName, packageName)
                appDetail.appImg = appIcon
                appsList.add(appDetail)
            }

            emit(appsList)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun addApp(app:AppTable){
        withContext(Dispatchers.IO){
            val aid = appDao.addApp(app)
            appDao.addAppSetting(AppSettingTable(aid.toInt(),0,0,1,""))
        }
    }

    suspend fun removeApp(app: AppTable){
        withContext(Dispatchers.IO){
            async {
                appDao.removeAppSetting(app.aId)
            }.join()
            appDao.removeApp(app)

        }
    }


    suspend fun addAppKeyword(keywordTable: KeywordTable){
        withContext(Dispatchers.IO){
            appDao.addKeyword(keywordTable)
        }
    }

    suspend fun deleteAppKeyword(keywordTable: KeywordTable){
        withContext(Dispatchers.IO){
            appDao.removeKeyword(keywordTable)
        }
    }

}

