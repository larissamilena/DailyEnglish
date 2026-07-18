package com.larissamilena.dailyenglish

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.larissamilena.dailyenglish.data.WordRepository
import com.larissamilena.dailyenglish.wallpaper.DailyWordWallpaperService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val word = WordRepository.getCurrentWord(this)
        findViewById<TextView>(R.id.textWord).text = word.word
        findViewById<TextView>(R.id.textTranslation).text = word.translation
        findViewById<TextView>(R.id.textExample).text = word.example
        findViewById<TextView>(R.id.textExampleTranslation).text = word.exampleTranslation

        findViewById<View>(R.id.buttonSetWallpaper).setOnClickListener {
            setLiveWallpaper()
        }
    }

    private fun setLiveWallpaper() {
        val component = ComponentName(this, DailyWordWallpaperService::class.java)
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
        }
        try {
            startActivity(intent)
        } catch (notFound: ActivityNotFoundException) {
            try {
                startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
            } catch (stillNotFound: ActivityNotFoundException) {
                Toast.makeText(this, R.string.wallpaper_picker_not_found, Toast.LENGTH_LONG).show()
            }
        }
    }
}
