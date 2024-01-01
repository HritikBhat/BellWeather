package com.hritikbhat.bellweather.data.db.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class KeywordTable(
    @ColumnInfo val aId : Int,
    @ColumnInfo val kName: String,
){
    @PrimaryKey(autoGenerate = true)
    var kId: Int = 0
}
