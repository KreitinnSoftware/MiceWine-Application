package com.micewine.emu.activities

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.MotionEvent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.micewine.emu.R
import com.micewine.emu.controller.ControllerUtils.connectedPhysicalControllers
import com.micewine.emu.controller.ControllerUtils.getAxisStatus
import com.micewine.emu.controller.ControllerUtils.updateAxisState
import com.micewine.emu.controller.ControllerUtils.updateButtonsState
import com.micewine.emu.databinding.ActivityControllerViewBinding
import com.micewine.emu.views.ControllerView
import com.micewine.emu.views.XInputOverlayView.Companion.A_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.B_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.LB_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.LEFT_ANALOG
import com.micewine.emu.views.XInputOverlayView.Companion.LS_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.LT_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.RB_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.RIGHT_ANALOG
import com.micewine.emu.views.XInputOverlayView.Companion.RS_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.RT_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.SELECT_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.START_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.X_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.Y_BUTTON

class ControllerTestActivity : AppCompatActivity() {
    private var binding: ActivityControllerViewBinding? = null
    private var controllerView: ControllerView? = null
    private var virtualControllerMapperDrawerLayout: DrawerLayout? = null
    private var navigationView: NavigationView? = null
    private var leftAnalogID = -1
    private var rightAnalogID = -1
    private var aButtonID = -1
    private var bButtonID = -1
    private var xButtonID = -1
    private var yButtonID = -1
    private var startButtonID = -1
    private var selectButtonID = -1
    private var ltButtonID = -1
    private var rtButtonID = -1
    private var rsButtonID = -1
    private var lsButtonID = -1
    private var rbButtonID = -1
    private var lbButtonID = -1

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityControllerViewBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        controllerView = findViewById(R.id.controllerView)

        virtualControllerMapperDrawerLayout = findViewById(R.id.virtualControllerMapperDrawerLayout)
        virtualControllerMapperDrawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        navigationView = findViewById(R.id.navigationView)
        navigationView?.setNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.exitButton -> {
                    finish()
                }
            }

            true
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        leftAnalogID = controllerView!!.analogList.indexOfFirst { it.id == LEFT_ANALOG }
        rightAnalogID = controllerView!!.analogList.indexOfFirst { it.id == RIGHT_ANALOG }
        aButtonID = controllerView!!.buttonList.indexOfFirst { it.id == A_BUTTON }
        bButtonID = controllerView!!.buttonList.indexOfFirst { it.id == B_BUTTON }
        xButtonID = controllerView!!.buttonList.indexOfFirst { it.id == X_BUTTON }
        yButtonID = controllerView!!.buttonList.indexOfFirst { it.id == Y_BUTTON }
        rbButtonID = controllerView!!.buttonList.indexOfFirst { it.id == RB_BUTTON }
        lbButtonID = controllerView!!.buttonList.indexOfFirst { it.id == LB_BUTTON }
        rsButtonID = controllerView!!.buttonList.indexOfFirst { it.id == RS_BUTTON }
        lsButtonID = controllerView!!.buttonList.indexOfFirst { it.id == LS_BUTTON }
        rtButtonID = controllerView!!.buttonList.indexOfFirst { it.id == RT_BUTTON }
        ltButtonID = controllerView!!.buttonList.indexOfFirst { it.id == LT_BUTTON }
        startButtonID = controllerView!!.buttonList.indexOfFirst { it.id == START_BUTTON }
        selectButtonID = controllerView!!.buttonList.indexOfFirst { it.id == SELECT_BUTTON }
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            updateAxisState(event)

            controllerView!!.analogList[leftAnalogID].fingerX = connectedPhysicalControllers.first().state.lx * controllerView!!.analogList[leftAnalogID].x
            controllerView!!.analogList[leftAnalogID].fingerY = connectedPhysicalControllers.first().state.ly * controllerView!!.analogList[leftAnalogID].y
            controllerView!!.analogList[rightAnalogID].fingerX = connectedPhysicalControllers.first().state.rx * controllerView!!.analogList[rightAnalogID].x
            controllerView!!.analogList[rightAnalogID].fingerY = connectedPhysicalControllers.first().state.ry * controllerView!!.analogList[rightAnalogID].y
            controllerView!!.dpadList[0].dpadStatus = getAxisStatus(connectedPhysicalControllers.first().state.dpadX, connectedPhysicalControllers.first().state.dpadY, 0.25F)
            controllerView!!.buttonList[rtButtonID].isPressed = connectedPhysicalControllers.first().state.rt > 0F
            controllerView!!.buttonList[ltButtonID].isPressed = connectedPhysicalControllers.first().state.lt > 0F
            controllerView!!.invalidate()
        }
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event == null) return true
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (virtualControllerMapperDrawerLayout?.isOpen!!) {
                virtualControllerMapperDrawerLayout?.closeDrawers()
            } else {
                virtualControllerMapperDrawerLayout?.openDrawer(GravityCompat.START)
            }
        }

        updateButtonsState(event)
        controllerView!!.buttonList[aButtonID].isPressed = connectedPhysicalControllers.first().state.aPressed
        controllerView!!.buttonList[bButtonID].isPressed = connectedPhysicalControllers.first().state.bPressed
        controllerView!!.buttonList[xButtonID].isPressed = connectedPhysicalControllers.first().state.xPressed
        controllerView!!.buttonList[yButtonID].isPressed = connectedPhysicalControllers.first().state.yPressed
        controllerView!!.buttonList[lbButtonID].isPressed = connectedPhysicalControllers.first().state.lbPressed
        controllerView!!.buttonList[rbButtonID].isPressed = connectedPhysicalControllers.first().state.rbPressed
        controllerView!!.buttonList[startButtonID].isPressed = connectedPhysicalControllers.first().state.startPressed
        controllerView!!.buttonList[selectButtonID].isPressed = connectedPhysicalControllers.first().state.selectPressed
        controllerView!!.buttonList[rsButtonID].isPressed = connectedPhysicalControllers.first().state.rsPressed
        controllerView!!.buttonList[lsButtonID].isPressed = connectedPhysicalControllers.first().state.lsPressed
        controllerView?.invalidate()

        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return onKeyDown(keyCode, event)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }
}