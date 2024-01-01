package com.hritikbhat.bellweather.data.sharedPref

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

class SharedPreferenceInstance(context: Context) {

    private var sharedPref: SharedPreferences
    private val myPrefName: String = "MY_PREFS"

    init {
        this.sharedPref = context.getSharedPreferences(myPrefName,
            AppCompatActivity.MODE_PRIVATE
        )
    }

    fun getSPInstance(): SharedPreferences {
        return this.sharedPref
    }





}