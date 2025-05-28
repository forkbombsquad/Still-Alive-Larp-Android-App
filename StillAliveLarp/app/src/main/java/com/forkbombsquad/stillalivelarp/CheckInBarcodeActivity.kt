package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone

import com.forkbombsquad.stillalivelarp.utils.BarcodeGenerator
import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.ifLet

class CheckInBarcodeActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var kvView: KeyValueView
    private lateinit var image: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_in_barcode)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.checkinbarcode_title)
        kvView = findViewById(R.id.checkinbarcode_keyvalueview)
        image = findViewById(R.id.checkinbarcode_image)
        buildView()
    }

    private fun buildView() {
        OldDataManager.shared.checkinBarcodeModel.ifLet({
            kvView.isGone = false
            image.isGone = false

            title.text = "Check In\n${it.player.fullName}"
            kvView.set("Checking In As", it.character?.fullName ?: "NPC")
            image.setImageBitmap(BarcodeGenerator.generateCheckInBarcode(it))
        }, {
            title.text = "Error Generating Barcode"
            kvView.isGone = true
            image.isGone = true
        })
    }

    override fun onBackPressed() {
        OldDataManager.shared.unrelaltedUpdateCallback()
        super.onBackPressed()
    }
}