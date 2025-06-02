package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import com.forkbombsquad.stillalivelarp.services.managers.DataManager

import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.github.chrisbanes.photoview.PhotoView

class SAImageViewActivity : NoStatusBarActivity() {

    private lateinit var photoView: PhotoView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sa_image_view)
        setupView()
    }

    private fun setupView() {
        photoView = findViewById(R.id.imageContainer)
        photoView.maximumScale = 100.0f
        DataManager.shared.treatingWounds.ifLet {
            photoView.setImageBitmap(it)
        }
    }
}