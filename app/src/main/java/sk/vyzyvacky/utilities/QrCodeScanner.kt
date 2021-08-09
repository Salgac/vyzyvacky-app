package sk.vyzyvacky.utilities

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import com.google.zxing.integration.android.IntentIntegrator
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
    }
}