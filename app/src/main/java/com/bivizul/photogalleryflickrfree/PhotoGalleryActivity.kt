package com.bivizul.photogalleryflickrfree

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PhotoGalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_gallery)

        // Проверяем, разместился ли фрагмент в контейнере, если нет, создаем экземпляр
        // PhotoGalleryFragment и добавляем его в контейнер
        val isFragmentContainerEmpty = savedInstanceState == null
        if (isFragmentContainerEmpty) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragmentContainer, PhotoGalleryFragment.newInstance())
                .commit()
        }
    }

    companion object {
        /** PollWorker будет вызывать функцию PhotoGalleryActivity.newIntent(...),
         * обертывать полученный интент в PendingIntent и устанавливать уведомление */
        fun newIntent(context: Context): Intent {
            return Intent(context, PhotoGalleryActivity::class.java)
        }
    }

}