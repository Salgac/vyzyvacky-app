package sk.vyzyvacky.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.drawer_header.*
import kotlinx.android.synthetic.main.drawer_header.view.*
import kotlinx.android.synthetic.main.titlebar_main.*
import kotlinx.android.synthetic.main.titlebar_main.view.*
import org.json.JSONObject
import sk.vyzyvacky.R
import sk.vyzyvacky.model.LogEntry
import sk.vyzyvacky.model.SKArrayAdapter
import sk.vyzyvacky.utilities.ConnectionType
import sk.vyzyvacky.utilities.DataHandler
import sk.vyzyvacky.utilities.NetworkUtil
import sk.vyzyvacky.utilities.QrCodeScanner
import java.sql.Timestamp
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var dataHandler: DataHandler
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerHeader: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //toolbar
        setSupportActionBar(mainToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //utilities
        dataHandler = DataHandler(this.applicationContext)
        resetAdapter()

        //drawer
        setupDrawer()

        //listeners
        submit_button.setOnClickListener {
            saveLogEntry()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection in toolbar
        when (item.itemId) {
            android.R.id.home -> {
                drawer_layout.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupDrawer() {
        drawerToggle = object : ActionBarDrawerToggle(this,
            drawer_layout,
            mainToolbar,
            R.string.drawer_open,
            R.string.drawer_close) {
            override fun onDrawerStateChanged(newState: Int) {
                super.onDrawerStateChanged(newState)
                if (newState == DrawerLayout.STATE_SETTLING) {
                    if (drawer_layout.isDrawerOpen(Gravity.LEFT)) {
                        //closing
                    } else {
                        //opening
                        updateConnectionStatus()
                    }
                }
            }
        }
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        drawerToggle.drawerArrowDrawable.color = ContextCompat.getColor(this, android.R.color.white)

        drawer_layout.addDrawerListener(drawerToggle)

        //nav menu setup
        nvView.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }

        logout_button.setOnClickListener {
            logout()
        }

        //insert data into header
        val game = dataHandler.getGame()!!
        val qrContent = JSONObject("{\"c\":\"" + game.code + "\",\"p\":\"" + game.password + "\"}")
        drawerHeader = nvView.getHeaderView(0)

        val qrImageView = drawerHeader.findViewById<ImageView>(R.id.headerQrImage)
        val codeTextView = drawerHeader.findViewById<TextView>(R.id.headerCode)
        val passwordTextView = drawerHeader.findViewById<TextView>(R.id.headerPassword)

        codeTextView.text = game.code
        qrImageView.setImageBitmap(QrCodeScanner.generate(qrContent.toString()))
        passwordTextView.setOnClickListener {
            passwordTextView.text = game.password
            Handler(Looper.getMainLooper()).postDelayed({
                passwordTextView.text = getString(R.string.drawer_pass_placeholder)
            }, 3000)
        }
    }

    private fun resetAdapter() {
        // Get the string array
        val nameList = dataHandler.getNamesForAdapter()

        // Create the adapter and set it to the AutoCompleteTextViews
        val adapter = SKArrayAdapter(this, android.R.layout.simple_list_item_1, nameList)
        autoCompleteTextView1.setAdapter(adapter)
        autoCompleteTextView2.setAdapter(adapter)
    }

    private fun selectDrawerItem(item: MenuItem) {
        when (item.itemId) {
            R.id.nav_view_database -> showDatabase()
            R.id.nav_view_log -> showLog()
            R.id.nav_import -> importDatabase()
            R.id.nav_export -> exportLogEntries()
            R.id.nav_settings -> showSettings()
            R.id.nav_about -> showAbout()
        }
        drawer_layout.closeDrawers()
    }

    private fun importDatabase() {
        dataHandler.importParticipants()
        resetAdapter()
    }

    private fun exportLogEntries() {
        dataHandler.exportEntries()
    }

    private fun showDatabase() {
        val databaseIntent = Intent(this@MainActivity, DatabaseActivity::class.java)
        startActivity(databaseIntent)
    }

    private fun showLog() {
        val logIntent = Intent(this@MainActivity, LogActivity::class.java)
        startActivity(logIntent)
    }

    private fun showSettings() {
        TODO()
    }

    private fun showAbout() {
        TODO()
    }

    private fun updateConnectionStatus() {
        if (NetworkUtil.getConnectivityStatus(this) != ConnectionType.NOT_CONNECTED) {
            connectionBar.text = getString(R.string.drawer_connection_ok)
            connectionBar.setBackgroundColor(Color.GREEN)
        } else {
            connectionBar.text = getString(R.string.drawer_connection_ko)
            connectionBar.setBackgroundColor(Color.RED)
        }
    }

    private fun logout() {
        dataHandler.removeGame()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun saveLogEntry() {
        val participants = dataHandler.getParticipants()
        if (participants.isEmpty()) {
            Toast.makeText(this, "Database not imported.", Toast.LENGTH_LONG).show()
            return
        }

        //get strings
        val time = Timestamp(System.currentTimeMillis())
        val winner = autoCompleteTextView1.text.toString()
        val looser = autoCompleteTextView2.text.toString()

        //reset values of inputs
        autoCompleteTextView1.setText("")
        autoCompleteTextView2.setText("")

        //test the input
        var winnerID: Int? = null
        var looserID: Int? = null
        for (i in participants.indices) {
            val current = participants[i]
            val currentName = current.lastname + " " + current.firstname
            if (winner == currentName) {
                //found winner
                winnerID = current.id
            }
            if (looser == currentName) {
                //found looser
                looserID = current.id
            }
        }
        if (winnerID == null || looserID == null) {
            //invalid entry, abort
            Toast.makeText(this, this.resources.getString(R.string.entry_failed), Toast.LENGTH_LONG)
                .show()
            return
        }

        //log an entry
        dataHandler.addEntry(LogEntry(time.toString(), winnerID, looserID))
        Toast.makeText(this, this.resources.getString(R.string.entry_added), Toast.LENGTH_LONG)
            .show()

        //change focus
        autoCompleteTextView1.isFocusableInTouchMode = true
        autoCompleteTextView1.requestFocus()
    }
}