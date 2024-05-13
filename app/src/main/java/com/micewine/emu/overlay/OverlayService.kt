package com.micewine.emu.overlay

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.constraintlayout.widget.ConstraintLayout
import com.micewine.emu.LorieView
import com.micewine.emu.R

class OverlayService : Service() {
    private val handler = Handler()
    private var windowManager: WindowManager? = null
    private var overlayLayout: ConstraintLayout? = null
    private var lorie: LorieView? = null
    private var isLongClick = false
    private val longClickStart = Runnable {
        isLongClick = true
        Log.v("longClickUpButtonLorie", "start")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        val inflater = LayoutInflater.from(this)
        overlayLayout = inflater.inflate(R.layout.overlay_layout, null) as ConstraintLayout
        val stopOverlayBtn = overlayLayout!!.findViewById<Button>(R.id.stopOverlayView)
        val btn_up = overlayLayout!!.findViewById<Button>(R.id.button_up)
        val btn_down = overlayLayout!!.findViewById<Button>(R.id.button_down)
        val mAlphaControls = overlayLayout!!.findViewById<SeekBar>(R.id.changeAlphaControls)
        mAlphaControls.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        lorie = LorieView(this)
        btn_up.setOnClickListener { v: View? ->
            try {
                lorie!!.sendKeyEvent(0, KeyEvent.KEYCODE_DPAD_UP, true)
                waitForEventSender(KeyEvent.KEYCODE_DPAD_UP)
            } catch (err: Exception) {
                Log.e("stderr for send key", err.toString())
            }
        }
        btn_up.setOnTouchListener { v: View?, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Ação quando o botão é pressionado
                    Log.v("inicio do btn", "btn pressionado")
                    handler.postDelayed(
                        longClickStart,
                        200
                    ) // Inicia um atraso para detectar o início do clique longo
                    lorie!!.sendKeyEvent(0, XKeyCodes.DPAD_UP, true)
                }

                MotionEvent.ACTION_UP -> {
                    // Ação quando o botão é liberado
                    if (isLongClick) {
                        // Ação quando o clique longo termina
                        isLongClick = false
                        waitForEventSender(XKeyCodes.DPAD_UP)
                        handler.removeCallbacksAndMessages(null) // Remove todos os callbacks para evitar que o clique curto seja detectado após o clique longo
                    } else {
                        lorie!!.sendKeyEvent(0, XKeyCodes.DPAD_UP, true)
                        waitForEventSender(XKeyCodes.DPAD_UP)
                    }
                    handler.removeCallbacks(longClickStart) // Remove o callback para o clique longo se o botão for liberado antes do clique longo ser detectado
                }
            }
            false
        }
        windowManager!!.addView(overlayLayout, params)
        stopOverlayBtn.setOnClickListener { v: View? -> stopSelf() }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (overlayLayout != null && windowManager != null) {
            windowManager!!.removeView(overlayLayout)
            overlayLayout = null
        }
    }

    private fun waitForEventSender(keyCode: Int) {
        val waitForEventSender = Runnable { lorie!!.sendKeyEvent(0, keyCode, false) }
        try {
            Thread.sleep(50)
        } catch (err: InterruptedException) {
        }
        waitForEventSender.run()
    }
}