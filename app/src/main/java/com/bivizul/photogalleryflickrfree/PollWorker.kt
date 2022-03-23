package com.bivizul.photogalleryflickrfree

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
        if (items.isEmpty()) {
            return Result.success()
        }
        val resultId = items.first().id
        if (resultId == lastResultId) {
            Log.i(TAG, "Got an old result: $resultId")
        } else {
            Log.i(TAG, "Got a new result: $resultId")
            QueryPreferences.setLastResultId(context, resultId)

            /** Научим PollWorker уведомлять пользователя о том, что новый результат готов,
            создав объект Notification и вызвав функцию
            NotificationManager.notify(Int, Notification)
            Добавим уведомление */
            val intent = PhotoGalleryActivity.newIntent(context)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            val resources = context.resources
            val notification = NotificationCompat
                /** Принимает ID канала и использует его для установки параметра канала уведомления,
                 * если пользователь запустил приложение на Oreo или выше. Если у пользователя
                 * запущена более ранняя версия Android, NotificationCompat.Builder игнорирует
                 * канал */
                .Builder(context, NOTIFICATION_CHANNEL_ID)
                .setTicker(resources.getString(R.string.new_pictures_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.new_pictures_title))
                .setContentText(resources.getString(R.string.new_pictures_text))
                /** Будет уничтожен, когда пользователь нажмет на уведомление на панели */
                .setContentIntent(pendingIntent)
                /** Уведомление также будет удалено из ящика уведомлений, когда пользователь нажмет
                 * на него */
                .setAutoCancel(true)
                .build()

            val notificationManager = NotificationManagerCompat.from(context)
            /** Размещения нашего уведомления */
            notificationManager.notify(0, notification)

            /** Отправка широковещательного интента с разрешением */
            context.sendBroadcast(Intent(ACTION_SHOW_NOTIFICATION), PERM_PRIVATE)
        }
        return Result.success()
    }

    companion object{
        const val ACTION_SHOW_NOTIFICATION = "com.bivizul.photogalleryflickrfree.SHOW_NOTIFICATION"
        const val PERM_PRIVATE = "com.bivizul.photogalleryflickrfree.PRIVATE"
    }
}