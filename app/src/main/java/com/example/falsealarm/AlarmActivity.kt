package com.example.falsealarm

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AlarmActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var button: Button
    private lateinit var preferences: Preferences
    private lateinit var vibrator: Vibrator
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        hideStatusBars()

        textView = findViewById(R.id.false_time)
        button = findViewById(R.id.wake_up)

        preferences = Preferences(this)

        textView.text = String.format("%02d:%02d", preferences.endHour, preferences.endMin)
        button.setOnClickListener { finish() }

        if (preferences.vibrateOn) {
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(longArrayOf(100, 200, 100, 200), 0)
        }

        mediaPlayer = MediaPlayer.create(this, preferences.ringtone)
        mediaPlayer.setVolume(1.0f, 1.0f)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (preferences.vibrateOn) {
            vibrator.cancel()
        }

        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }

    private fun hideStatusBars() {
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        if (Build.VERSION.SDK_INT >= 30) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }
}
