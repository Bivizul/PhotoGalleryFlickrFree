package com.bivizul.photogalleryflickrfree.api

import com.bivizul.photogalleryflickrfree.GalleryItem
import com.google.gson.annotations.SerializedName

class PhotoResponse {
    @SerializedName("photo")
    // Хранит список галерейных объектов и примечаний к нему с помощью @SerializedName("photo")
    lateinit var galleryItems: List<GalleryItem>
}