package sk.vyzyvacky.utilities

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.CaptureActivity
import org.json.JSONException
import org.json.JSONObject
import sk.vyzyvacky.R

class QrCodeScanner {
    companion object {
        fun scan(activity: Activity, resources: Resources) {
            val scanIntegrator = IntentIntegrator(activity)
            scanIntegrator.setPrompt(resources.getString(R.string.login_qr_prompt))
            scanIntegrator.setBeepEnabled(false)
            scanIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
            scanIntegrator.captureActivity = CaptureActivity::class.java
            scanIntegrator.setOrientationLocked(true)
            scanIntegrator.setBarcodeImageEnabled(true)
            scanIntegrator.initiateScan()
        }

        fun getOutput(requestCode: Int, resultCode: Int, data: Intent?): JSONObject? {
            val scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (scanningResult != null) {
                if (scanningResult.contents != null) {
                    val content = scanningResult.contents.toString()
                    return try {
                        JSONObject(content)
                    } catch (e: JSONException) {
                        null
                    }
                }
            }
            return JSONObject()
        }

        fun generate(content: String): Bitmap {
            try {
                // generate a 500x500 QR code
                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 500, 500)

                val height: Int = bitMatrix.height
                val width: Int = bitMatrix.width
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bmp.setPixel(x,
                            y,
                            if (bitMatrix.get(x, y)) Color.BLACK else Color.parseColor("#fafafa"))
                    }
                }
                if (bmp != null) {
                    return bmp
                }
            } catch (e: WriterException) {
            }
            return Bitmap.createBitmap(0, 0, Bitmap.Config.RGB_565)
        }
    }
}