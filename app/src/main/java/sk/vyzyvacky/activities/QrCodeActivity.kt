package sk.vyzyvacky.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_qr_code.*
import org.json.JSONObject
import sk.vyzyvacky.R
import sk.vyzyvacky.utilities.QrCodeManager
import sk.vyzyvacky.utilities.data.DataHandler

class QrCodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code)

        generateContent()
    }

    private fun generateContent() {
        val dataHandler = DataHandler(this.applicationContext)
        val game = dataHandler.getGame()!!

        val qrContent = JSONObject("{\"c\":\"" + game.code + "\",\"p\":\"" + game.password + "\"}")

        qrView.setImageBitmap(QrCodeManager.generate(qrContent.toString()))
    }
}