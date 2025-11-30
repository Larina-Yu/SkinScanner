package com.example.skinscanner

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.activity.result.contract.ActivityResultContracts

class GalleryFragment : Fragment() {

    // activity result launcher for picking images from the gallery
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Navigate to result screen, pass the URI as string
            findNavController().navigate("result/${it}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)

        // Button to select an image
        val button = view.findViewById<Button>(R.id.selectImageButton)
        button.setOnClickListener {
            //launch image picker
            pickImageLauncher.launch("image/*") // open gallery
        }

        return view
    }
}
