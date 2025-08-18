package com.forkbombsquad.stillalivelarp.utils

import android.graphics.Bitmap
import android.os.Bundle
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.views.rules.RulesFragment

import com.github.chrisbanes.photoview.PhotoView

class SAImageViewActivity : NoStatusBarActivity() {

    private lateinit var photoView: PhotoView
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sa_image_view)
        setupView()
    }

    private fun setupView() {
        bitmap = DataManager.shared.getPassedData(RulesFragment::class, DataManagerPassedDataKey.IMAGE)!!
        photoView = findViewById(R.id.imageContainer)
        photoView.maximumScale = 100.0f
        buildView()
    }

    private fun buildView() {
        photoView.setImageBitmap(bitmap)
    }
}