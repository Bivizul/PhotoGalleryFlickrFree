package com.bivizul.photogalleryflickrfree.api

/** Этот класс будет сопоставлен с крайним объектом в JSON-данных (тот, который находится в
верхней части иерархии JSON-объектов с соответствующим обозначением { }) */
class FlickrResponse {
    lateinit var photos: PhotoResponse
}