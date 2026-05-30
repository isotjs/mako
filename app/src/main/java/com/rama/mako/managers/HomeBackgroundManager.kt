package com.rama.mako.managers

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.rama.mako.R

class HomeBackgroundManager(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = PrefsManager.getInstance(appContext)
    private val wallpaperManager by lazy { WallpaperManager.getInstance(appContext) }

    fun applyTo(view: View, modeOverride: String? = null) {
        val mode = modeOverride ?: prefs.getHomeBackgroundMode()
        view.background = createBackgroundDrawable(mode)
    }

    fun applyToSettings(view: View, modeOverride: String? = null) {
        applyTo(view, modeOverride)
    }

    fun createWallpaperOverlayDrawable(): Drawable {
        val strength = prefs.getHomeBackgroundScreenOpacityStrength() // 0..9
        val alpha = (strength * 0x99) / 9
        return ColorDrawable(ColorUtils.setAlphaComponent(Color.BLACK, alpha))
    }

    fun createBackgroundDrawable(mode: String): Drawable {
        return when (mode) {
            PrefsManager.BackgroundMode.WALLPAPER -> ColorDrawable(
                ContextCompat.getColor(
                    appContext,
                    R.color.bg_1
                )
            )

            else -> ColorDrawable(ContextCompat.getColor(appContext, R.color.bg_1))
        }
    }

    fun getWallpaperSignature(): Int? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return null
        return runCatching {
            wallpaperManager.getWallpaperId(WallpaperManager.FLAG_SYSTEM)
        }.getOrNull()
    }

    private fun darkenForReadability(color: Int): Int {
        var tuned = ColorUtils.blendARGB(color, Color.BLACK, 0.62f)
        var iterations = 0

        while (ColorUtils.calculateLuminance(tuned) > 0.15 && iterations < 5) {
            tuned = ColorUtils.blendARGB(tuned, Color.BLACK, 0.25f)
            iterations++
        }

        return tuned
    }
}