package com.example.falsealarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var startPicker: TimePicker
    private lateinit var endPicker: TimePicker
    private lateinit var pendingIntent: PendingIntent
    private lateinit var alarmManager: AlarmManager
    private lateinit var calendar: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent("alarm_event")
        if (Build.VERSION.SDK_INT >= 26) {
            intent.component =
                ComponentName(applicationContext, "com.example.falsealarm.AlarmReceiver")
        }
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)

        timeInit()

        startPicker.setOnTimeChangedListener { _, _, _ -> savePreferences() }
        endPicker.setOnTimeChangedListener { _, _, _ -> savePreferences() }
    }

    private fun timeInit() {
        startPicker = findViewById(R.id.start_time)
        endPicker = findViewById(R.id.end_time)
        startPicker.setIs24HourView(true)
        endPicker.setIs24HourView(true)

        loadPreferences()

        calendar = Calendar.getInstance()
        calendar.set(Calendar.SECOND, 0)

        savePreferences()
    }

    private fun savePreferences() {
        val sharedPreferences = getSharedPreferences("fa_time", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putInt("start_hour", startPicker.currentHour)
        editor.putInt("start_min", startPicker.currentMinute)
        editor.putInt("end_hour", endPicker.currentHour)
        editor.putInt("end_min", endPicker.currentMinute)
        editor.apply()

        alarmManager.set(AlarmManager.RTC_WAKEUP, getRandomTime(), pendingIntent)
    }

    private fun loadPreferences() {
        val sharedPreferences = getSharedPreferences("fa_time", MODE_PRIVATE)

        startPicker.currentHour = sharedPreferences.getInt("start_hour", startPicker.currentHour + 1)
        startPicker.currentMinute = sharedPreferences.getInt("start_min", startPicker.currentMinute)
        endPicker.currentHour = sharedPreferences.getInt("end_hour", endPicker.currentHour + 9)
        endPicker.currentMinute = sharedPreferences.getInt("end_min", endPicker.currentMinute)
    }

    private fun getRandomTime(): Long {
        val currentTime = System.currentTimeMillis()

        calendar.set(Calendar.HOUR_OF_DAY, startPicker.currentHour)
        calendar.set(Calendar.MINUTE, startPicker.currentMinute)
        while (calendar.timeInMillis >= currentTime) {
            calendar.set(Calendar.HOUR_OF_DAY, startPicker.currentHour - 24)
        }
        calendar.set(Calendar.HOUR_OF_DAY, startPicker.currentHour + 24)
        val startTime = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, endPicker.currentHour)
        calendar.set(Calendar.MINUTE, endPicker.currentMinute)
        if (calendar.timeInMillis < startTime) {
            calendar.set(Calendar.HOUR_OF_DAY, endPicker.currentHour + 24)
        }
        val endTime = calendar.timeInMillis

        return (startTime..endTime).random()
    }
}
