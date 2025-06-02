package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.PlayerCheckOutBarcodeModel
import com.forkbombsquad.stillalivelarp.tabbar_fragments.HomeFragment

import com.forkbombsquad.stillalivelarp.utils.BarcodeGenerator
import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.ifLet

class CheckOutBarcodeActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var kvView: KeyValueView
    private lateinit var image: ImageView

    private var barcode: PlayerCheckOutBarcodeModel? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out_barcode)
        setupView()
    }


    private fun setupView() {
        title = findViewById(R.id.checkoutbarcode_title)
        kvView = findViewById(R.id.checkoutbarcode_keyvalueview)
        image = findViewById(R.id.checkoutbarcode_image)
        barcode = DataManager.shared.getPassedData(HomeFragment::class, DataManagerPassedDataKey.CHECKOUT_BARCODE)
        buildView()
    }

    private fun buildView() {
        barcode.ifLet({
            kvView.isGone = false
            image.isGone = false

            title.text = "Check Out\n${it.player.fullName}"
            kvView.set("Checking Out", it.character?.fullName ?: "NPC")
            image.setImageBitmap(BarcodeGenerator.generateCheckOutBarcode(it))
        }, {
            title.text = "Error Generating Barcode"
            kvView.isGone = true
            image.isGone = true
        })
    }

    override fun onBackPressed() {
        DataManager.shared.callUpdateCallback(HomeFragment::class)
        super.onBackPressed()
    }
}