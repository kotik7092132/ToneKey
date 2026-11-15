package com.example.ime

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.model.KeyboardSettings

class KeyboardFeedbackHelper(private val context: Context) {

  private val audioManager: AudioManager? by lazy {
    context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
  }

  @Suppress("DEPRECATION")
  private val vibrator: Vibrator? by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      manager?.defaultVibrator
    } else {
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }

  @Suppress("DEPRECATION")
  fun triggerKeyFeedback(settings: KeyboardSettings, isSpaceOrAction: Boolean = false) {
    if (settings.hapticEnabled) {
      val duration = settings.hapticDurationMs.toLong().coerceIn(5L, 40L)
      val vib = vibrator
      if (vib != null && vib.hasVibrator()) {
        runCatching {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitude = if (isSpaceOrAction) 170 else 130
            vib.vibrate(VibrationEffect.createOneShot(duration, amplitude))
          } else {
            vib.vibrate(duration)
          }
        }
      }
    }
    if (settings.soundEnabled) {
      runCatching {
        val effect =
          if (isSpaceOrAction) AudioManager.FX_KEYPRESS_SPACEBAR else AudioManager.FX_KEYPRESS_STANDARD
        audioManager?.playSoundEffect(effect, 0.55f)
      }
    }
  }

  fun triggerTickFeedback(settings: KeyboardSettings) {
    if (!settings.hapticEnabled) return
    val vib = vibrator ?: return
    if (!vib.hasVibrator()) return
    runCatching {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vib.vibrate(VibrationEffect.createOneShot(8L, 90))
      } else {
        @Suppress("DEPRECATION")
        vib.vibrate(8L)
      }
    }
  }
}
