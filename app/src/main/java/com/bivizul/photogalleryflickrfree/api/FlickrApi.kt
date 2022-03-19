package com.bivizul.photogalleryflickrfree.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface FlickrApi {
//
//    /** Определение запроса «получить недавние интересные фотографии»
//
//    * Путь и другая информация, которую вы включили в аннотацию @GET, будет добавлена в
//     * URL-адрес Retrofit до выдачи веб-запроса. */
//    @GET("services/rest/?method=flickr.interestingness.getList" +
//            "&api_key=606eea7a5b7bbfadf0dee62657c7223f" +
//            "&format=json" +
//            "&nojsoncallback=1" +
//            "&extras=url_s")

    @GET("services/rest?method=flickr.interestingness.getList")
    fun fetchPhotos(): Call<FlickrResponse>

    /** Принимает на вход строку с URL-адресом и возвращает исполняемый объект вызова
     *
    На вход подается URL-адрес, который используется для определения того, откуда загружать
    данные. Использование беспараметрической аннотации @GET в сочетании с аннотацией первого
    параметра в fetchUrlBytes(...) с @Url приводит к тому, что Retrofit полностью
    переопределяет базовый URL. Вместо этого Retrofit будет использовать URL,
    переданный в функцию fetchUrlBytes(...) */
    @GET
    fun fetchUrlBytes(@Url url: String): Call<ResponseBody>

    /** Добавление функции поиска
     *
     * Аннотация @Query позволяет динамически добавлять к URL параметры запроса.
    В данном случае мы добавляем параметр запроса text. Значение, присваиваемое
    параметру, зависит от аргумента, переданного в searchPhotos(String). Например,
    вызов searchPhotos("robot") добавит в URL приписку text=robot */
    @GET("services/rest?method=flickr.photos.search")
    fun searchPhotos(@Query("text") query: String): Call<FlickrResponse>
}
