package com.example.falsealarm

import android.app.*
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import java.util.*

class MainActivity : Activity() {
    private lateinit var alarmSwitch: ToggleButton
    private lateinit var vibrateSwitch: ToggleButton
    private lateinit var ringtoneSelect: Button
    private lateinit var startRow: LinearLayout
    private lateinit var endRow: LinearLayout
    private lateinit var startTime: TextView
    private lateinit var endTime: TextView
    private lateinit var preferences: Preferences
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        alarmSwitch = findViewById(R.id.alarm_switch)
        vibrateSwitch = findViewById(R.id.vibrate_switch)
        ringtoneSelect = findViewById(R.id.ringtone)
        startRow = findViewById(R.id.start_row)
        endRow = findViewById(R.id.end_row)
        startTime = findViewById(R.id.start_time)
        endTime = findViewById(R.id.end_time)

        alarmManagerInit()
        loadPreferences()
        listenersInit()
    }

    override fun onResume() {
        super.onResume()

        Handler().postDelayed({
            if (preferences.alarmOn) {
                setAlarm()
            }
        }, 2333)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        preferences.ringtone = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
    }

    private fun alarmManagerInit() {
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent("alarm_event")
        if (Build.VERSION.SDK_INT >= 26) {
            intent.component =
                ComponentName(applicationContext, "com.example.falsealarm.AlarmReceiver")
        }
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
    }

    private fun loadPreferences() {
        preferences = Preferences(this)

        alarmSwitch.isChecked = preferences.alarmOn
        vibrateSwitch.isChecked = preferences.vibrateOn

        startTime.text = String.format("%02d:%02d", preferences.startHour, preferences.startMin)
        endTime.text = String.format("%02d:%02d", preferences.endHour, preferences.endMin)
        if (preferences.alarmOn) {
            alarmSwitch.setBackgroundResource(R.drawable.ic_alarm_on_blue_48dp)
            startRow.visibility = View.VISIBLE
            endRow.visibility = View.VISIBLE
            setAlarm()
        } else {
            alarmSwitch.setBackgroundResource(R.drawable.ic_alarm_off_gray_48dp)
            startRow.visibility = View.GONE
            endRow.visibility = View.GONE
        }
        if (preferences.vibrateOn) {
            vibrateSwitch.setBackgroundResource(R.drawable.ic_vibration_on_blue_48dp)
        } else {
            vibrateSwitch.setBackgroundResource(R.drawable.ic_vibration_off_gray_48dp)
        }
    }

    private fun listenersInit() {
        alarmSwitch.setOnCheckedChangeListener { _, b ->
            preferences.alarmOn = b
            if (b) {
                startRow.visibility = View.VISIBLE
                endRow.visibility = View.VISIBLE
                alarmSwitch.setBackgroundResource(R.drawable.ic_alarm_on_blue_48dp)
                setAlarm()
            } else {
                alarmSwitch.setBackgroundResource(R.drawable.ic_alarm_off_gray_48dp)
                startRow.visibility = View.GONE
                endRow.visibility = View.GONE
                alarmManager.cancel(pendingIntent)
            }
        }
        vibrateSwitch.setOnCheckedChangeListener { _, b ->
            preferences.vibrateOn = b
            if (b) {
                vibrateSwitch.setBackgroundResource(R.drawable.ic_vibration_on_blue_48dp)
            } else {
                vibrateSwitch.setBackgroundResource(R.drawable.ic_vibration_off_gray_48dp)
            }
        }
        ringtoneSelect.setOnClickListener {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            startActivityForResult(intent, 0)
        }
        startRow.setOnClickListener {
            TimePickerDialog(this, AlertDialog.THEME_HOLO_LIGHT, OnTimeSetListener { _, i, i2 ->
                preferences.startHour = i
                preferences.startMin = i2
                startTime.text =
                    String.format("%02d:%02d", preferences.startHour, preferences.startMin)
                setAlarm()
            }, preferences.startHour, preferences.startMin, true).show()
        }
        endRow.setOnClickListener {
            TimePickerDialog(this, AlertDialog.THEME_HOLO_LIGHT, OnTimeSetListener { _, i, i2 ->
                preferences.endHour = i
                preferences.endMin = i2
                endTime.text = String.format("%02d:%02d", preferences.endHour, preferences.endMin)
                setAlarm()
            }, preferences.endHour, preferences.endMin, true).show()
        }
    }

    private fun setAlarm() {
        when {
            Build.VERSION.SDK_INT >= 23 -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    getRandomTime(),
                    pendingIntent
                )
            }
            Build.VERSION.SDK_INT >= 19 -> {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, getRandomTime(), pendingIntent)
            }
            else -> {
                alarmManager.set(AlarmManager.RTC_WAKEUP, getRandomTime(), pendingIntent)
            }
        }
    }

    private fun getRandomTime(): Long {
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.SECOND, 0)

        calendar.set(Calendar.HOUR_OF_DAY, preferences.startHour)
        calendar.set(Calendar.MINUTE, preferences.startMin)
        while (calendar.timeInMillis >= currentTime) {
            calendar.set(Calendar.HOUR_OF_DAY, preferences.startHour - 24)
        }
        calendar.set(Calendar.HOUR_OF_DAY, preferences.startHour + 24)
        val startTime = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, preferences.endHour)
        calendar.set(Calendar.MINUTE, preferences.endMin)
        if (calendar.timeInMillis < startTime) {
            calendar.set(Calendar.HOUR_OF_DAY, preferences.endHour + 24)
        }
        val endTime = calendar.timeInMillis

        return (startTime..endTime).random()
    }
}
