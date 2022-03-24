package com.bivizul.photogalleryflickrfree

import android.net.Uri
import com.google.gson.annotations.SerializedName

data class GalleryItem(
    var title: String = "",
    var id: String = "",
    // Переопределение сопоставления имен-свойств по умолчанию
    @SerializedName("url_s") var url: String = "",
    @SerializedName("owner") var owner: String = ""
) {
    /** Добавление кода страницы фотографии.
     * Для определения URL-адреса фотографии создается новое свойство owner и добавляется
     * вычисляемое свойство photoPageUri для генерации URL-адресов страницы фото*/
    val photoPageUri: Uri
        get() {
            return Uri.parse("https://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(owner)
                .appendPath(id)
                .build()
        }
}
