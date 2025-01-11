package com.micewine.emu.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.appRootDir
import com.micewine.emu.activities.MainActivity.Companion.miceWineVersion
import com.micewine.emu.activities.MainActivity.Companion.ratPackagesDir
import com.micewine.emu.activities.MainActivity.Companion.selectedBox64
import com.micewine.emu.activities.MainActivity.Companion.usrDir
import com.micewine.emu.core.EnvVars.getEnv
import com.micewine.emu.core.ShellLoader.runCommandWithOutput
import com.micewine.emu.core.WineWrapper
import com.micewine.emu.databinding.FragmentAboutBinding
import java.io.File

class AboutFragment: Fragment() {
    private var binding: FragmentAboutBinding? = null
    private var rootView: View? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAboutBinding.inflate(inflater, container, false)
        rootView = binding!!.root

        rootView?.findViewById<TextView>(R.id.ApplicationVersion)?.apply {
            text = "App Version: $miceWineVersion"
        }

        rootView?.findViewById<TextView>(R.id.RootfsVersion)?.apply {
            text = "RootFS Version: ${File("$ratPackagesDir/rootfs-pkg-header").readLines()[2].substringAfter("=").replace("(", "(git-")}"
        }

        rootView?.findViewById<TextView>(R.id.Box64Version)?.apply {
            text = "Box64 Version: ${runCommandWithOutput("$ratPackagesDir/$selectedBox64/files/usr/bin/box64 -v").replace("\n", "")}"
        }

        rootView?.findViewById<TextView>(R.id.WineVersion)?.apply {
            text = "Wine Version: ${WineWrapper.wine("--version", true)}"
        }

        return rootView
    }
}