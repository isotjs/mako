package com.rama.mako.managers

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.core.graphics.ColorUtils

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
        val palette = ThemeManager.paletteFor(prefs.getTheme(), appContext)
        return ColorDrawable(palette.bg_1)
    }

    fun getWallpaperSignature(): Int? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return null
        return runCatching {
            wallpaperManager.getWallpaperId(WallpaperManager.FLAG_SYSTEM)
        }.getOrNull()
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

    private fun resolveWallpaperScrimColor(): Int {
        val fallback = ContextCompat.getColor(appContext, R.color.bg_wallpaper_scrim)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return fallback

        val wallpaperColors = runCatching {
            wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
        }.getOrNull() ?: return fallback

        val hints = wallpaperColors.colorHints
        val alpha = when {
            hints and WallpaperColors.HINT_SUPPORTS_DARK_TEXT != 0 -> 0xB8
            hints and WallpaperColors.HINT_SUPPORTS_DARK_THEME != 0 -> 0x7A
            else -> 0x96
        }

        return ColorUtils.setAlphaComponent(Color.BLACK, alpha)
    }

    private fun supportsWallpaperReactiveBackground(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
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