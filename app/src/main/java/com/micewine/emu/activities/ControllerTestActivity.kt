package com.micewine.emu.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.micewine.emu.R
import com.micewine.emu.controller.ControllerUtils.updateAxisState
import com.micewine.emu.controller.ControllerUtils.updateButtonsState
import com.micewine.emu.databinding.ActivityControllerViewBinding
import com.micewine.emu.fragments.ControllerViewFragment

class ControllerTestActivity : AppCompatActivity() {
    private var binding: ActivityControllerViewBinding? = null
    private var controllerViewToolbar: Toolbar? = null
    private var backButton: ImageButton? = null

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityControllerViewBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        controllerViewToolbar = findViewById(R.id.controllerViewTitle)
        controllerViewToolbar?.title = resources.getString(R.string.controller_view_title)

        backButton = findViewById(R.id.backButton)
        backButton?.setOnClickListener {
            onKeyDown(KeyEvent.KEYCODE_BACK, null)
        }

        fragmentLoader(ControllerViewFragment(), true)
    }

    private fun fragmentLoader(fragment: Fragment, appInit: Boolean) {
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out
            )
            replace(R.id.controller_view_content, fragment)

            if (!appInit) {
                addToBackStack(null)
            }

            commit()
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event == null) return true
        updateAxisState(event)

        supportFragmentManager.fragments.forEach {
            (it as ControllerViewFragment).invalidateControllerView()
        }

        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
                controllerViewToolbar?.title = resources.getString(R.string.controller_view_title)
            } else {
                finish()
            }
        }

        if (event == null) return true
        updateButtonsState(event)
        supportFragmentManager.fragments.forEach {
            (it as ControllerViewFragment).invalidateControllerView()
        }

        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return onKeyDown(keyCode, event)
    }
}