package com.bivizul.photogalleryflickrfree

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters


private const val TAG = "PollWorker"

class PollWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    /** Функция doWork() вызывается из фонового потока, поэтому можно выполнять
    в ней любые долгосрочные задачи. Возвращаемые значения функции указывают
    на состояние работы */
    override fun doWork(): Result {
        Log.i(TAG, "Work request triggered")
        /** Получение последних фотографий */
        val query = QueryPreferences.getStoredQuery(context)
        val lastResultId = QueryPreferences.getLastResultId(context)
        val items: List<GalleryItem> = if (query.isEmpty()) {
            FlickrFetchr().fetchPhotosRequest()
                .execute()
                .body()
                ?.photos
                ?.galleryItems
        } else {
            FlickrFetchr().searchPhotosRequest(query)
                .execute()
                .body()
                ?.photos
                ?.galleryItems
        } ?: emptyList()
        /** Проверка, нет ли новых фотографий */
        if(items.isEmpty()){
            return Result.success()
        }
        val resultId = items.first().id
        if(resultId == lastResultId){
            Log.i(TAG,"Got an old result: $resultId")
        } else {
            Log.i(TAG, "Got a new result: $resultId")
            QueryPreferences.setLastResultId(context, resultId)
        }
        return Result.success()
    }
}