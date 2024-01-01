package com.hritikbhat.bellweather.data.db.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

import android.graphics.Bitmap
import android.os.Build
import android.os.Parcel
import android.os.Parcelable

@Entity
data class AppTable(
    @ColumnInfo val aName: String,
    @ColumnInfo val aPackageName: String,
) : Parcelable {

    @PrimaryKey(autoGenerate = true)
    var aId: Int = 0

    @Ignore
    var appImg: Bitmap? = null

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    ) {
        aId = parcel.readInt()
        // Read the Bitmap from the parcel
        appImg = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            parcel.readParcelable(Bitmap::class.java.classLoader, Bitmap::class.java)
        } else {
            parcel.readParcelable(Bitmap::class.java.classLoader)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(aName)
        parcel.writeString(aPackageName)
        parcel.writeInt(aId)
        // Write the Bitmap to the parcel
        parcel.writeParcelable(appImg, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AppTable> {
        override fun createFromParcel(parcel: Parcel): AppTable {
            return AppTable(parcel)
        }

        override fun newArray(size: Int): Array<AppTable?> {
            return arrayOfNulls(size)
        }
    }
}
