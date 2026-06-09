package com.onlyfreeai.app.util

import android.util.Log
import com.onlyfreeai.app.BuildConfig

object Logger {
    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg)
        }
    }

    fun i(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, msg)
        }
    }

    fun w(tag: String, msg: String, tr: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (tr != null) {
                Log.w(tag, msg, tr)
            } else {
                Log.w(tag, msg)
            }
        }
    }

    fun e(tag: String, msg: String, tr: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (tr != null) {
                Log.e(tag, msg, tr)
            } else {
                Log.e(tag, msg)
            }
        }
    }
}
