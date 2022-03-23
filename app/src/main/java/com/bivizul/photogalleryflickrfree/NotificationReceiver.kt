package com.bivizul.photogalleryflickrfree

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

private const val TAG = "NotificationReceiver"

/** Широковещательный приемник */
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "received brodcast: ${intent.action}")
    }
}