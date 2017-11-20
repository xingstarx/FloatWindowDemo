package com.star.window

import android.app.Service
import android.content.Intent
import android.os.IBinder

class FloatWindowService : Service() {
    private var floatWindow: FloatWindow? = null
    private var isShow = false

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        show()
        return super.onStartCommand(intent, flags, startId)
    }

    fun show() {
        if (!isShow) {
            floatWindow = FloatWindow(this)?.apply {
                show()
                isShow = true
            }
        }
    }


}
