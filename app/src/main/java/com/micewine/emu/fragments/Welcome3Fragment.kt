package com.micewine.emu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.extractedAssets

class Welcome3Fragment : Fragment() {
    private var extractingFilesText: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_welcome_3, container, false)

        extractingFilesText = rootView.findViewById(R.id.extractingFilesText)

        if (extractedAssets) {
            extractingFilesText?.text = getText(R.string.extracted_resources_text)
        } else {
            extractingFilesText?.text = getText(R.string.extracting_resources_text)
        }

        return rootView
    }
}
