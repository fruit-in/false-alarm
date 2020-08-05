package com.example.falsealarm

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var startTime: TextView
    private lateinit var endTime: TextView
    private lateinit var alarmSwitch: ToggleButton
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var pendingIntent: PendingIntent
    private lateinit var alarmManager: AlarmManager
    private lateinit var calendar: Calendar
    private var startHour = 0
    private var startMin = 0
    private var endHour = 0
    private var endMin = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        alarmSwitch = findViewById(R.id.alarm_switch)

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent("alarm_event")
        if (Build.VERSION.SDK_INT >= 26) {
            intent.component =
                ComponentName(applicationContext, "com.example.falsealarm.AlarmReceiver")
        }
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)

        timeInit()

        alarmSwitch.setOnCheckedChangeListener { _, b ->
            startTime.isClickable = b
            endTime.isClickable = b
            val editor = sharedPreferences.edit()
            editor.putBoolean("alarm_on", alarmSwitch.isChecked)
            editor.apply()
            if (b) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, getRandomTime(), pendingIntent)
            } else {
                alarmManager.cancel(pendingIntent)
            }
        }
        startTime.setOnClickListener {
            TimePickerDialog(this, AlertDialog.THEME_HOLO_LIGHT, OnTimeSetListener { _, i, i2 ->
                startHour = i
                startMin = i2
                startTime.text = String.format("%02d:%02d", startHour, startMin)
                savePreferences()
            }, startHour, startMin, true).show()
        }
        endTime.setOnClickListener {
            TimePickerDialog(this, AlertDialog.THEME_HOLO_LIGHT, OnTimeSetListener { _, i, i2 ->
                endHour = i
                endMin = i2
                endTime.text = String.format("%02d:%02d", endHour, endMin)
                savePreferences()
            }, endHour, endMin, true).show()
        }

        startTime.isClickable = alarmSwitch.isChecked
        endTime.isClickable = alarmSwitch.isChecked
    }

    private fun timeInit() {
        startTime = findViewById(R.id.start_time)
        endTime = findViewById(R.id.end_time)

        sharedPreferences = getSharedPreferences("fa_time", MODE_PRIVATE)
        loadPreferences()

        calendar = Calendar.getInstance()
        calendar.set(Calendar.SECOND, 0)

        savePreferences()
    }

    private fun savePreferences() {
        val editor = sharedPreferences.edit()

        editor.putBoolean("alarm_on", alarmSwitch.isChecked)
        editor.putInt("start_hour", startHour)
        editor.putInt("start_min", startMin)
        editor.putInt("end_hour", endHour)
        editor.putInt("end_min", endMin)
        editor.apply()

        if (alarmSwitch.isChecked) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, getRandomTime(), pendingIntent)
        }
    }

    private fun loadPreferences() {
        alarmSwitch.isChecked = sharedPreferences.getBoolean("alarm_on", false)
        startHour = sharedPreferences.getInt("start_hour", 0)
        startMin = sharedPreferences.getInt("start_min", 0)
        endHour = sharedPreferences.getInt("end_hour", 0)
        endMin = sharedPreferences.getInt("end_min", 0)

        startTime.text = String.format("%02d:%02d", startHour, startMin)
        endTime.text = String.format("%02d:%02d", endHour, endMin)
    }

    private fun getRandomTime(): Long {
        val currentTime = System.currentTimeMillis()

        calendar.set(Calendar.HOUR_OF_DAY, startHour)
        calendar.set(Calendar.MINUTE, startMin)
        while (calendar.timeInMillis >= currentTime) {
            calendar.set(Calendar.HOUR_OF_DAY, startHour - 24)
        }
        calendar.set(Calendar.HOUR_OF_DAY, startHour + 24)
        val startTime = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, endHour)
        calendar.set(Calendar.MINUTE, endMin)
        if (calendar.timeInMillis < startTime) {
            calendar.set(Calendar.HOUR_OF_DAY, endHour + 24)
        }
        val endTime = calendar.timeInMillis

        return (startTime..endTime).random()
    }
}
