package com.micewine.emu.activities

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.micewine.emu.R
import com.micewine.emu.controller.XKeyCodes.getXKeyScanCodes
import com.micewine.emu.databinding.ActivityVirtualControllerMapperBinding
import com.micewine.emu.views.OverlayViewCreator
import com.micewine.emu.views.OverlayViewCreator.VirtualButton
import com.micewine.emu.views.OverlayViewCreator.VirtualAnalog

class VirtualControllerOverlayMapper : AppCompatActivity() {
    private var binding: ActivityVirtualControllerMapperBinding? = null
    private var overlayView: OverlayViewCreator? = null
    private var virtualControllerMapperDrawerLayout: DrawerLayout? = null
    private var navigationView: NavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVirtualControllerMapperBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        overlayView = findViewById(R.id.overlayView)

        virtualControllerMapperDrawerLayout = findViewById(R.id.virtualControllerMapperDrawerLayout)

        virtualControllerMapperDrawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        navigationView = findViewById(R.id.navigationView)

        navigationView?.setNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.addButton -> {
                    overlayView?.addButton(
                        VirtualButton(
                            overlayView?.buttonList?.count()!! + 1, "",
                            overlayView?.width!! / 2F,
                            overlayView?.height!! / 2F,
                            180F,
                            getXKeyScanCodes("Enter")
                        )
                    )

                    virtualControllerMapperDrawerLayout?.closeDrawers()
                }

                R.id.addVAxis -> {
                    overlayView?.addAnalog(
                        VirtualAnalog(overlayView?.analogList?.count()!! + 1,
                            overlayView?.width!! / 2F,
                            overlayView?.height!! / 2F,
                            0F,
                            0F,
                            250F,
                            getXKeyScanCodes("Up"),
                            getXKeyScanCodes("Down"),
                            getXKeyScanCodes("Left"),
                            getXKeyScanCodes("Right"),
                            false
                        )
                    )

                    virtualControllerMapperDrawerLayout?.closeDrawers()
                }

                R.id.exitButton -> {
                    overlayView?.saveOnPreferences()

                    finish()
                }
            }

            true
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (virtualControllerMapperDrawerLayout?.isOpen!!) {
                virtualControllerMapperDrawerLayout?.closeDrawers()
            } else {
                virtualControllerMapperDrawerLayout?.openDrawer(GravityCompat.START)
            }

            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()

        overlayView?.saveOnPreferences()
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