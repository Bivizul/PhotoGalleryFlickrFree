package com.bivizul.photogalleryflickrfree

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit

/** Значение PREF_SEARCH_QUERY используется в качестве ключа для хранения запроса.
Этот ключ применяется во всех операциях чтения или записи запроса */
private const val PREF_SEARCH_QUERY = "searchQuery"
private const val PREF_LAST_RESULT_ID = "lastResultId"

/** Добавление файла для работы с хранимым запросом
 *
 * Приложению нужен только один экземпляр QueryPreferences, который
может использоваться всеми другими компонентами. Из-за этого мы используем
ключевое слово object (вместо class), чтобы указать, что QueryPreferences — это
синглтон*/
object QueryPreferences {
    fun getStoredQuery(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PREF_SEARCH_QUERY, "")!!
    }

    fun setStoredQuery(context: Context, query: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putString(PREF_SEARCH_QUERY, query)
            }
    }

    /** Сохранение и получение идентификатора последней фотографии */
    fun setLastResultId(context: Context, lastResultId: String){
        PreferenceManager.getDefaultSharedPreferences(context).edit(){
            putString(PREF_LAST_RESULT_ID, lastResultId)
        }
    }

    fun getLastResultId(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PREF_LAST_RESULT_ID, "")!!
    }



}