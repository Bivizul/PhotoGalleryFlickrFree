package com.bivizul.photogalleryflickrfree

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ThumbnailDownloader"

/** Для идентификации сообщений как запросов на загрузку */
private const val MESSAGE_DOWNLOAD = 0

class ThumbnailDownloader<in T>(
    private val responseHandler: Handler,
    private val onThumbnailDownloaded: (T, Bitmap) -> Unit
) : HandlerThread(TAG) {

    val fragmentLifecycleObserver: LifecycleObserver = object : LifecycleObserver {
        /** Используется аннотация @OnLifecycleEvent(Lifecycle.
        Event), позволяющая ассоциировать функцию в вашем классе с обратным вызовом жизненного цикла.
        Lifecycle.Event.ON_CREATE регистрирует вызов функции
        ThumbnailDownloader.setup() при вызове функции LifecycleOwner.onCreate(...).
        Lifecycle.Event.ON_DESTROY регистрирует вызов функции ThumbnailDownloader.
        tearDown()при вызове функции LifecycleOwner.onDestroy()
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE) */
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun setup() {
            Log.i(TAG, "Starting background thread")
            start()
            looper
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun tearDown() {
            Log.i(TAG, "Destroing background thread")
            // Остановка потока ThumbnailDownloader
            quit()
        }
    }

    /** Добавление наблюдателя жизненного цикла представления */
    val viewLifecycleObserver: LifecycleObserver =
        object : LifecycleObserver{
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun tearDown(){
                Log.i(TAG,"Clearing all requests from queue")
                requestHandler.removeMessages(MESSAGE_DOWNLOAD)
                requestMap.clear()
            }
        }

    private var hasQuit = false

    /** Хранит ссылку на объект Handler, отвечающий за постановку в очередь запросов на загрузку
    в фоновом потоке ThumbnailDownloader. Этот объект также будет отвечать за обработку сообщений
    запросов на загрузку при извлечении их из очереди */
    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<T, String>()

    /** В свойстве flickrFetchr хранится ссылка на экземпляр FlickrFetchr. Таким образом,
    весь код установки Retrofit будет выполняться только один раз в течение жизни
    потока */
    private val flickrFetchr = FlickrFetchr()

    /** При проверке сообщает Lint, что мы приводим msg.obj к типу T без предварительной проверки
     * того, относится ли msg.obj к этому типу на самом деле */
    @Suppress("UNCHECKED_CAST")
    /** Предупреждение HandlerLeak убирается аннотацией @SuppressLint("HandlerLeak"), так как
     * создаваемый обработчик прикреплен к looper фонового потока */
    @SuppressLint("HandlerLeak")
    /** Вызывается до того, как Looper впервые проверит очередь, поэтому она хорошо подходит для
     * создания реализации Handler */
    override fun onLooperPrepared() {
        requestHandler = object : Handler() {
            /** Проверяем тип сообщения, читаем значение obj (которое имеет тип T и служит
             * идентификатором для запроса) и передаем его функции handleRequest(...).
             * (Вспомним, что Handler.handleMessage(...) будет вызываться, когда сообщение
             * загрузки извлечено из очереди и готово к обработке.) */
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    val target = msg.obj as T
                    Log.i(TAG, "Got a request for URL: ${requestMap[target]}")
                    handleRequest(target)
                }
            }
        }
    }

    // Завершение потока
    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

    fun queueThumbnail(target: T, url: String) {
        Log.i(TAG, "Got a URL: $url")
        requestMap[target] = url
        /** Постановка нового сообщения в очередь сообщений фонового потока */
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
            .sendToTarget()
    }

    fun clearQueue() {
        requestHandler.removeMessages(MESSAGE_DOWNLOAD)
        requestMap.clear()
    }

    private fun handleRequest(target: T) {
        val url = requestMap[target] ?: return
        val bitmap = flickrFetchr.fetchPhoto(url) ?: return
        /** Загрузка и вывод изображений */
        responseHandler.post(Runnable {
            /**Эта проверка гарантирует, что каждый объект PhotoHolder получит правильное
            изображение, даже если за прошедшее время был сделан другой запрос.
             * Проверяется hasQuit. Если выполнение ThumbnailDownloader уже завершилось,
             * выполнение каких-либо обратных вызовов небезопасно */
            if (requestMap[target] != url || hasQuit) {
                return@Runnable
            }
            /** Удаляем из requestMap связь «PhotoHolder —URL» и назначаем изображение для
            PhotoHolder */
            requestMap.remove(target)
            onThumbnailDownloaded(target, bitmap)
        })
    }
}