package com.bivizul.photogalleryflickrfree

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bivizul.photogalleryflickrfree.api.FlickrApi
import com.bivizul.photogalleryflickrfree.api.FlickrResponse
import com.bivizul.photogalleryflickrfree.api.PhotoInterceptor
import com.bivizul.photogalleryflickrfree.api.PhotoResponse
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "FlickrFetchr"

class FlickrFetchr {

    private val flickrApi: FlickrApi

    init {
        /** Добавление перехватчика в конфигурацию Retrofit */
        val client = OkHttpClient.Builder()
            .addInterceptor(PhotoInterceptor())
            .build()

        /** Retrofit.Builder() — это интерфейс, выполняющий настройку и сборку вашего
        экземпляра Retrofit. Базовый URL для вашей конечной точки задается с помощью функции
        baseUrl(...). Здесь надо указать главную страницу.

        Вызов функции build() возвращает экземпляр Retrofit, у которого появляются настройки,
        заданные с помощью объекта builder. */
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            // Добавление конвертера в объект Retrofit
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()


        /** При вызове функции retrofit.create(...) Retrofit использует информацию в указанном
        интерфейсе API наряду с информацией, указанной при сборке экземпляра Retrofit,
        для создания экземпляров анонимного класса, реализующего интерфейс «на лету» */
        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        return fetchPhotoMetadata(flickrApi.fetchPhotos())
    }

    /** Добавление функции поиска */
    fun searchPhotos(query: String): LiveData<List<GalleryItem>> {
        return fetchPhotoMetadata(flickrApi.searchPhotos(query))
    }

    // Функия оборачивает функию API Retrofit
    private fun fetchPhotoMetadata(flickrRequest: Call<FlickrResponse>)
            : LiveData<List<GalleryItem>> {
        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()

        /** Для выполнения веб-запроса, содержащегося в объекте Call, необходимо вызвать функцию
        enqueue(...) в onCreate(...) и передать экземпляр retrofit2.Callback
        Выполнение запроса асинхронно */
        flickrRequest.enqueue(object : Callback<FlickrResponse> {
            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }

            override fun onResponse(
                call: Call<FlickrResponse>,
                response: Response<FlickrResponse>
            ) {
                Log.d(TAG, "Response received: ${response.body()}")
                // После успешного завершения результат становится публичным путем установки
                // значения responseLiveData.value
                val flickrResponse: FlickrResponse? = response.body()
                val photoResponse: PhotoResponse? = flickrResponse?.photos
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems ?: mutableListOf()
                galleryItems = galleryItems.filterNot {
                    it.url.isNullOrBlank()
                }
                responseLiveData.value = galleryItems

            }
        })
        return responseLiveData
    }

    /** Загружаем данные по заданномуURL- адресу и декодируем их в изображение Bitmap

    Аннотация @WorkerThread указывает, что эта функция должна вызываться только
    в фоновом потоке.  */
    @WorkerThread
    fun fetchPhoto(url: String): Bitmap? {
        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
        val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
        Log.i(TAG, "Decoded bitmap = $bitmap from Response = $response")
        return bitmap
    }
}
