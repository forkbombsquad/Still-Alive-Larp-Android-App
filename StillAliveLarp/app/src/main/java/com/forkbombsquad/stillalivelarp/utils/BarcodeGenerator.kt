package com.forkbombsquad.stillalivelarp.utils

import android.graphics.Bitmap
import com.forkbombsquad.stillalivelarp.services.models.CheckInOutBarcodeModel
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class BarcodeGenerator {
    companion object {
        fun generateCheckInBarcode(model: CheckInOutBarcodeModel): Bitmap {
            return generateBarcodeFromModel(model)
        }

        fun generateCheckOutBarcode(model: CheckInOutBarcodeModel): Bitmap {
            return generateBarcodeFromModel(model)
        }

        private fun generateBarcodeFromModel(model: Any): Bitmap {
            val content = globalToJson(model)
            val bc = BarcodeEncoder()
            return bc.encodeBitmap(content, BarcodeFormat.QR_CODE, 1000, 1000)
        }
    }
}