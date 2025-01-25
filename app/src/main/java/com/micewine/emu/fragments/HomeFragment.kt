package com.micewine.emu.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.miceWineVersion
import com.micewine.emu.activities.MainActivity.Companion.ratPackagesDir
import com.micewine.emu.activities.MainActivity.Companion.selectedBox64
import com.micewine.emu.core.ShellLoader.runCommandWithOutput
import com.micewine.emu.core.WineWrapper
import com.micewine.emu.databinding.FragmentHomeBinding
import java.io.File

class HomeFragment: Fragment() {
    private var binding: FragmentHomeBinding? = null
    private var rootView: View? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        rootView = binding!!.root

        rootView?.findViewById<TextView>(R.id.ApplicationVersion)?.apply {
            text = "$miceWineVersion"
        }

        rootView?.findViewById<TextView>(R.id.RootfsVersion)?.apply {
            text = "${File("$ratPackagesDir/rootfs-pkg-header").readLines()[2].substringAfter("=").replace("(", "(git-")}"
        }

        rootView?.findViewById<TextView>(R.id.Box64Version)?.apply {
            text = "${runCommandWithOutput("$ratPackagesDir/$selectedBox64/files/usr/bin/box64 -v").replace("\n", "")}"
        }

        rootView?.findViewById<TextView>(R.id.WineVersion)?.apply {
            text = "${WineWrapper.wine("--version", true)}"
        }

        return rootView
    }
}