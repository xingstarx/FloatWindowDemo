package com.star.window

import android.content.Context
import android.util.TypedValue

object UIUtils {
    fun dp2px(dp: Float, mContext: Context): Int {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                mContext.resources.displayMetrics).toInt()
    }
}