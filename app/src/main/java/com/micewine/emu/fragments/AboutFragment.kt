package com.micewine.emu.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.miceWineVersion
import com.micewine.emu.activities.MainActivity.Companion.ratPackagesDir
import com.micewine.emu.databinding.FragmentAboutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AboutFragment : Fragment() {
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
            text = miceWineVersion
        }

        rootView?.findViewById<TextView>(R.id.RootfsVersion)?.apply {
            lifecycleScope.launch(Dispatchers.IO) {
                val rootFsVersionFile = File("$ratPackagesDir/rootfs-pkg-header")
                val newText = if (rootFsVersionFile.exists()) {
                    rootFsVersionFile.readLines()[2].substringAfter("=").replace("(", "(git-")
                } else {
                    "???"
                }

                withContext(Dispatchers.Main) {
                    text = newText
                }
            }
        }

        return rootView
    }
}
