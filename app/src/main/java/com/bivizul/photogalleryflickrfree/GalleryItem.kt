package com.bivizul.photogalleryflickrfree

import com.google.gson.annotations.SerializedName

data class GalleryItem(
    var title: String = "",
    var id: String = "",
    // Переопределение сопоставления имен-свойств по умолчанию
    @SerializedName("url_s") var url: String = ""
)
