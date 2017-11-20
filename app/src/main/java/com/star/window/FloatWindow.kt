package com.star.window

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.*

class FloatWindow(private var context: Context) : View.OnTouchListener {

    private var wmParams: WindowManager.LayoutParams? = null
    private var wManager: WindowManager? = null
    private var screenWidth = 0
    private var screenHeight = 0
    private var statusBarHeight = 0
    private var isShow = false
    private var view: View? = null
    private var downRawX = 0f
    private var downRawY = 0f
    private var xPosition = 0
    private var yPosition = 0
    private var viewWidth = 0
    private var animating: Boolean = false

    private fun initFloatWindow() {
        wmParams = WindowManager.LayoutParams().apply {
            statusBarHeight = UIUtils.dp2px(25f, context)
            wManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wManager?.apply {
                screenWidth = defaultDisplay.width
                screenHeight = defaultDisplay.height
            }
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (Build.VERSION.SDK_INT > 23) {
                    //在android7.1以上系统需要使用TYPE_PHONE类型 配合运行时权限
                    WindowManager.LayoutParams.TYPE_PHONE
                } else {
                    WindowManager.LayoutParams.TYPE_TOAST
                }
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            format = PixelFormat.RGBA_8888
            gravity = Gravity.LEFT or Gravity.TOP
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            x = screenWidth
            y = (screenHeight - statusBarHeight) / 2 / 3
            alpha = 1f
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
    }

    private fun initView() {
        view = LayoutInflater.from(context).inflate(R.layout.view_float_window, null).apply {
            setOnTouchListener(this@FloatWindow)
        }
        viewWidth = UIUtils.dp2px(56f, context)
    }


    fun show() {
        if (!isShow) {
            wManager?.addView(view, wmParams)
            isShow = true
        }
    }

    fun hide() {
        if (isShow) {
            wManager?.removeView(view)
            isShow = false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (animating) {
            return true
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                xPosition = wmParams?.x ?: 0
                yPosition = wmParams?.y ?: 0
                downRawX = event.rawX
                downRawY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                if (Math.abs(event.rawY - downRawY) > 10 || Math.abs(event.rawX - downRawX) > 10) {
                    val newX = xPosition + (event.rawX - downRawX).toInt()
                    val newY = yPosition + (event.rawY - downRawY).toInt()
                    updateWindowPosition(newX, newY)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val oldX = wmParams?.x ?: 0
                if (oldX + viewWidth / 2 < screenWidth / 2) {
                    snap(oldX, -10)
                } else {
                    snap(oldX, screenWidth - viewWidth + 10)
                }
            }
        }
        return true
    }

    private fun snap(fromX: Int, toX: Int) {
        val snapAnimator = ValueAnimator.ofFloat(0f, 1f)
        snapAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                animating = true
            }

            override fun onAnimationEnd(animation: Animator) {
                animating = false
            }
        })
        snapAnimator.addUpdateListener({ animation ->
            val currX = fromX + (animation.animatedFraction * (toX - fromX)).toInt()
            updateWindowPosition(currX, wmParams?.y ?: 0)
        })
        snapAnimator.start()
    }


    private fun updateWindowPosition(a: Int, b: Int) {
        var x = a
        var y = b
        if (isShow) {
            if (x < 0) {
                x = 0
            } else if (x > screenWidth - viewWidth) {
                x = screenWidth - viewWidth
            }
            if (y < 0) {
                y = 0
            } else if (y > screenHeight - viewWidth) {
                y = screenHeight - viewWidth
            }
            wmParams?.x = x
            wmParams?.y = y
            wManager?.updateViewLayout(view, wmParams)
        }
    }

    init {
        initFloatWindow()
        initView()
    }

}