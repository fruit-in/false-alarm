package com.example.falsealarm

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.Vibrator
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AlarmActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var button: Button
    private lateinit var ringtone: Uri
    private lateinit var vibrator: Vibrator
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        if (Build.VERSION.SDK_INT >= 30) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "fa:aa"
        )
        wakeLock.acquire(5000)

        val sharedPreferences = getSharedPreferences("fa_time", MODE_PRIVATE)
        val hour = sharedPreferences.getInt("end_hour", 0)
        val min = sharedPreferences.getInt("end_min", 0)

        textView = findViewById(R.id.false_time)
        textView.text = String.format("%02d:%02d", hour, min)
        button = findViewById(R.id.wake_up)
        button.setOnClickListener { finish() }

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(longArrayOf(100, 200, 100, 200), 0)

        ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        mediaPlayer = MediaPlayer.create(this, ringtone)
        mediaPlayer.setVolume(1.0f, 1.0f)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        vibrator.cancel()
        mediaPlayer.release()
        wakeLock.release()
    }
}
