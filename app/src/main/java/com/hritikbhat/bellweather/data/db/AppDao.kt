package com.hritikbhat.bellweather.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hritikbhat.bellweather.data.db.tables.AppSettingTable
import com.hritikbhat.bellweather.data.db.tables.AppTable
import com.hritikbhat.bellweather.data.db.tables.KeywordTable

@Dao
interface AppDao {
    @Insert
    fun addApp(app: AppTable): Long

    @Query("SELECT * FROM apptable ORDER BY aid DESC")
    fun getAllApps() : List<AppTable>

    @Query("SELECT * FROM apptable WHERE aid = :aId")
    fun getApp(aId: Int) : AppTable

    @Delete
    fun removeApp(app: AppTable)

    @Insert
    fun addAppSetting(appSetting: AppSettingTable)

    @Delete
    fun removeAppSetting(appSetting: AppSettingTable)

    @Query("DELETE FROM appsettingtable WHERE aId = :aId")
    fun removeAppSetting(aId: Int)


    @Query("SELECT * FROM appsettingtable WHERE asid = :aSId")
    fun getAppSetting(aSId:Int) : AppSettingTable

    @Query("SELECT * FROM appsettingtable WHERE aid = :aId")
    fun getAppSettingByAID(aId:Int) : AppSettingTable

    @Update
    fun updateAppSetting(appSetting: AppSettingTable)

    @Insert
    fun addKeyword(keyword: KeywordTable)

    @Delete
    fun removeKeyword(keyword: KeywordTable)

    @Query("SELECT * FROM keywordtable WHERE aid = :aId")
    fun getAllAppKeywords(aId:Int) : List<KeywordTable>

    @Query("SELECT * FROM keywordtable WHERE  :aSearchString LIKE '%' || keywordtable.kName || '%'")
    fun getAllAppKeywordsBySearchString(aSearchString: String): List<KeywordTable>


//


}
