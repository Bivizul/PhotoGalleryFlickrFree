package com.bivizul.photogalleryflickrfree.api

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

private const val API_KEY = "606eea7a5b7bbfadf0dee62657c7223f"

class PhotoInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // доступ к исходному запросу
        val originalRequest: Request = chain.request()

        /** Функция originalRequest.url() извлекает исходный URL из запроса, а затем
        используется HttpUrl.Builder для добавления параметров запроса.
        HttpUrl.Builder создает новый запрос на основе оригинального запроса и заменяет исходный
        URL на новый. Наконец, мы вызываем функцию chain.continue(newRequest)
        для создания ответа. Если вы не вызывали chain.continue(...), сетевой запрос не
        будет выполнен.
         */
        val newUrl: HttpUrl = originalRequest.url().newBuilder()
            .addQueryParameter("api_key", API_KEY)
            .addQueryParameter("format", "json")
            .addQueryParameter("nojsoncallback", "1")
            .addQueryParameter("extras", "url_s")
            .addQueryParameter("safesearch", "1")
            .addQueryParameter("license", "7")
            .addQueryParameter("sort", "interestingness-desc")
            .build()

        val newRequest: Request = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}