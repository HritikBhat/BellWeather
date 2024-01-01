package com.hritikbhat.bellweather.data.db.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AppSettingTable(
    @ColumnInfo val aId : Int,
    @ColumnInfo var isNotificationEnabled: Int,
    @ColumnInfo var isSMSEnabled: Int,
    @ColumnInfo var ringORSpeech: Int,
    @ColumnInfo var ringtonePath: String,
){
    @PrimaryKey(autoGenerate = true)
    var aSId: Int = 0
}
