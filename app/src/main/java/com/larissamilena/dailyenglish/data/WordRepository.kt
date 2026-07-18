package com.larissamilena.dailyenglish.data

import android.content.Context
import org.json.JSONArray
import java.util.Calendar

object WordRepository {

    @Volatile
    private var cachedWords: List<Word>? = null

    private fun loadWords(context: Context): List<Word> {
        cachedWords?.let { return it }
        synchronized(this) {
            cachedWords?.let { return it }
            val json = context.applicationContext.assets
                .open("words.json")
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            val array = JSONArray(json)
            val words = ArrayList<Word>(array.length())
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                words.add(
                    Word(
                        word = obj.getString("word"),
                        translation = obj.getString("translation"),
                        example = obj.getString("example"),
                        exampleTranslation = obj.getString("exampleTranslation")
                    )
                )
            }
            cachedWords = words
            return words
        }
    }

    /**
     * Deterministic pick based on day of year and AM/PM half, so the word changes
     * automatically every 12 hours (at local midnight and noon) without any
     * background work or storage. The bank has one word per half-day of the year.
     */
    fun getCurrentWord(context: Context): Word {
        val words = loadWords(context)
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val isPm = calendar.get(Calendar.AM_PM) == Calendar.PM
        val halfDaySlot = (dayOfYear - 1) * 2 + (if (isPm) 1 else 0)
        val index = halfDaySlot % words.size
        return words[index]
    }
}
