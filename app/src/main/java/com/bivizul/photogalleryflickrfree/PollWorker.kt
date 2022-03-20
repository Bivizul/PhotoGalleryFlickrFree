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
        return Result.success()
    }
}