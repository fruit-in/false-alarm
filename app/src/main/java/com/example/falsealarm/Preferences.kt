package com.example.falsealarm

import android.app.Activity
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri

class Preferences constructor(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("fa_time", Activity.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    var alarmOn
        set(value) {
            editor.putBoolean("alarm_on", value)
            editor.apply()
        }
        get() = sharedPreferences.getBoolean("alarm_on", false)
    var vibrateOn
        set(value) {
            editor.putBoolean("vibrate_on", value)
            editor.apply()
        }
        get() = sharedPreferences.getBoolean("vibrate_on", false)
    var ringtone: Uri?
        set(value) {
            editor.putString("ringtone", value.toString())
            editor.apply()
        }
        get() {
            val s = sharedPreferences.getString("ringtone", "")
            return if (s!!.startsWith("content://media/")) {
                Uri.parse(s)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }
        }
    var startHour
        set(value) {
            editor.putInt("start_hour", value)
            editor.apply()
        }
        get() = sharedPreferences.getInt("start_hour", 0)
    var startMin
        set(value) {
            editor.putInt("start_min", value)
            editor.apply()
        }
        get() = sharedPreferences.getInt("start_min", 0)
    var endHour
        set(value) {
            editor.putInt("end_hour", value)
            editor.apply()
        }
        get() = sharedPreferences.getInt("end_hour", 0)
    var endMin
        set(value) {
            editor.putInt("end_min", value)
            editor.apply()
        }
        get() = sharedPreferences.getInt("end_min", 0)
}
