package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CheckInOutBarcodeModel
import com.forkbombsquad.stillalivelarp.tabbar_fragments.HomeFragment

import com.forkbombsquad.stillalivelarp.utils.BarcodeGenerator
import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.ifLet

class CheckOutBarcodeActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var kvView: KeyValueView
    private lateinit var image: ImageView

    private lateinit var barcode: CheckInOutBarcodeModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out_barcode)
        setupView()
    }


    private fun setupView() {
        title = findViewById(R.id.checkoutbarcode_title)
        kvView = findViewById(R.id.checkoutbarcode_keyvalueview)
        image = findViewById(R.id.checkoutbarcode_image)
        barcode = DataManager.shared.getPassedData(HomeFragment::class, DataManagerPassedDataKey.BARCODE)!!
        buildView()
    }

    private fun buildView() {
        val player = DataManager.shared.players.firstOrNull { it.id == barcode.playerId }
        val character = DataManager.shared.getCharacter(barcode.characterId ?: -1)
        player.ifLet({
            kvView.isGone = false
            image.isGone = false

            title.text = "Check Out\n${it.fullName}"
            kvView.set("Checking Out", character?.fullName ?: "NPC")
            image.setImageBitmap(BarcodeGenerator.generateCheckOutBarcode(barcode))
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