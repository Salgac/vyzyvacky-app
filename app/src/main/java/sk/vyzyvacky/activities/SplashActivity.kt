package sk.vyzyvacky.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import sk.vyzyvacky.R
import sk.vyzyvacky.utilities.DataHandler

class SplashActivity : AppCompatActivity() {
    private lateinit var dataHandler: DataHandler

    private val SPLASH_TIME_OUT: Long = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //update participant database
        dataHandler = DataHandler(this.applicationContext)

        Handler().postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, SPLASH_TIME_OUT)
    }
}