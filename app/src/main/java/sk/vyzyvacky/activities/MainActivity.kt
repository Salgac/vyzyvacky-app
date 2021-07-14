package sk.vyzyvacky.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import sk.vyzyvacky.R
import sk.vyzyvacky.model.LogEntry
import sk.vyzyvacky.model.SKArrayAdapter
import sk.vyzyvacky.utilities.DataHandler
import java.sql.Timestamp
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var dataHandler: DataHandler

    private lateinit var mTextInput1: AutoCompleteTextView
    private lateinit var mTextInput2: AutoCompleteTextView
    private lateinit var button: Button
    private lateinit var mToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTextInput1 = findViewById(R.id.autoCompleteTextView1)
        mTextInput2 = findViewById(R.id.autoCompleteTextView2)
        button = findViewById(R.id.submit_button)

        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)

        dataHandler = DataHandler(this.applicationContext)
        resetAdapter()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        if (item.itemId == R.id.options) {
            settings()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun resetAdapter() {
        // Get the string array
        val nameList = dataHandler.getNamesForAdapter()

        // Create the adapter and set it to the AutoCompleteTextViews
        val adapter = SKArrayAdapter(this, android.R.layout.simple_list_item_1, nameList)
        mTextInput1.setAdapter(adapter)
        mTextInput2.setAdapter(adapter)
    }

    private fun settings() {
        val view = findViewById<View>(R.id.options)
        val popup = PopupMenu(this@MainActivity, view)
        popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)

        //listen for click
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.title.toString()) {
                this.resources.getString(R.string.menu_export) -> {
                    exportLogEntries()
                }
                this.resources.getString(R.string.menu_import) -> {
                    importDatabase()
                }
                this.resources.getString(R.string.menu_database_view) -> {
                    showDatabase()
                }
                this.resources.getString(R.string.menu_log_view) -> {
                    showLog()
                }
                this.resources.getString(R.string.menu_log_delete) -> {
                    deleteLast()
                }
                else -> return@setOnMenuItemClickListener true
            }
            true
        }
        popup.show()
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

    private fun deleteLast() {
        //get last log index
        val log = dataHandler.getEntries()
        val index = log.size - 1
        if (index < 0) {
            Toast.makeText(this, "No logs to delete.", Toast.LENGTH_LONG).show()
            return
        }

        //get Strings
        val last = log[index]
        val time = last.time
        val winner = last.let { dataHandler.getFullParticipantNameByID(it.winner) }
        val looser = last.let { dataHandler.getFullParticipantNameByID(it.looser) }
        val context: Context = this

        //popup
        AlertDialog.Builder(context)
            .setTitle("Zmazať posledný?")
            .setMessage("Si si istý že chceš zmazať tento log?\n$time:\n$winner vs. $looser")
            // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton(android.R.string.yes) { _: DialogInterface?, _: Int ->
                // Continue with delete operation
                dataHandler.removeEntry(index)
                Toast.makeText(context, "Last log entry removed successfully.", Toast.LENGTH_LONG)
                    .show()
            } // A null listener allows the button to dismiss the dialog and take no further action.
            .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }

    fun onClickBtn(v: View?) {
        val participants = dataHandler.getParticipants()
        if (participants.isEmpty()) {
            Toast.makeText(this, "Database not imported.", Toast.LENGTH_LONG).show()
            return
        }

        //get strings
        val time = Timestamp(System.currentTimeMillis())
        val winner = mTextInput1.text.toString()
        val looser = mTextInput2.text.toString()

        //reset values of inputs
        mTextInput1.setText("")
        mTextInput2.setText("")

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
        mTextInput1.isFocusableInTouchMode = true
        mTextInput1.requestFocus()
    }
}