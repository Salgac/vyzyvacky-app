package sk.vyzyvacky.activities

import android.annotation.SuppressLint
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
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import sk.vyzyvacky.R
import sk.vyzyvacky.data.Data
import sk.vyzyvacky.model.LogEntry
import sk.vyzyvacky.model.SKArrayAdapter
import sk.vyzyvacky.xml.XMLExport
import sk.vyzyvacky.xml.XMLImport
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var database: Data? = null
    private var importXML: XMLImport? = null
    private var exportXML: XMLExport? = null
    private var mTextInput1: AutoCompleteTextView? = null
    private var mTextInput2: AutoCompleteTextView? = null
    private var button: Button? = null
    private var mToolbar: Toolbar? = null
    private var imported = false
    private var isImporting = false
    private var isViewing = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUiViews()
        database = Data()
        importXML = XMLImport(this, this, database)
        exportXML = XMLExport(this, this, database)
        imported = false
        isImporting = false
        isViewing = false
        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)

        //launch importer
        importXML!!.execute()
    }

    override fun onDestroy() {
        if (!isImporting || !isViewing) exportXML!!.execute()
        super.onDestroy()
    }

    private fun initUiViews() {
        mTextInput1 = findViewById(R.id.autoCompleteTextView1)
        mTextInput2 = findViewById(R.id.autoCompleteTextView2)
        button = findViewById(R.id.submit_button)
        val nameList = arrayOf<String>()
        // Create the adapter and set it to the AutoCompleteTextViews
        val adapter = SKArrayAdapter(this, android.R.layout.simple_list_item_1, nameList)
        mTextInput1?.setAdapter(adapter)
        mTextInput2?.setAdapter(adapter)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val filePath = data!!.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)!!
            isImporting = false
            importXML!!.ParseXML(filePath)
        }
    }

    fun resetAdapter() {
        imported = true

        // Get the string array
        val nameList = database!!.namesForAdapter

        // Create the adapter and set it to the AutoCompleteTextViews
        val adapter = SKArrayAdapter(this, android.R.layout.simple_list_item_1, nameList)
        mTextInput1!!.setAdapter(adapter)
        mTextInput2!!.setAdapter(adapter)
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

    private fun settings() {
        val view = findViewById<View>(R.id.options)
        val popup = PopupMenu(this@MainActivity, view)
        popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)

        //listen for click
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.title.toString()) {
                "Import" -> {
                    isImporting = true
                    importXML!!.execute()
                }
                "Export" -> exportXML!!.execute()
                "Zobraz databázu" -> {
                    isViewing = true
                    showDatabase()
                    isViewing = false
                }
                "Zobraz log" -> {
                    isViewing = true
                    showLog()
                    isViewing = false
                }
                "Zmaž posledný" -> deleteLast()
                else -> return@setOnMenuItemClickListener true
            }
            true
        }
        popup.show()
    }

    fun showDatabase() {
        val databaseIntent = Intent(this@MainActivity, DatabaseActivity::class.java)
        databaseIntent.putExtra("data", database)
        startActivity(databaseIntent)
    }

    fun showLog() {
        val logIntent = Intent(this@MainActivity, LogActivity::class.java)
        logIntent.putExtra("data", database)
        startActivity(logIntent)
    }

    fun deleteLast() {
        //get last log index
        val index = database!!.log.size - 1
        if (index < 0) {
            Toast.makeText(this, "No logs to delete.", Toast.LENGTH_LONG).show()
            return
        }

        //get Strings
        val last = database!!.log[index]
        val time = last.time
        val winner = database!!.getFullParticipantNameByID(last.winner)
        val looser = database!!.getFullParticipantNameByID(last.looser)
        val context: Context = this

        //popup
        AlertDialog.Builder(context)
                .setTitle("Delete entry")
                .setMessage("Are you sure you want to delete this entry?\n$time $winner vs. $looser")
                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes) { dialog: DialogInterface?, which: Int ->
                    // Continue with delete operation
                    database!!.log.removeAt(index)
                    Toast.makeText(context, "Last log entry removed successfully.", Toast.LENGTH_LONG).show()
                } // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }

    fun onClickBtn(v: View?) {
        if (!imported) {
            Toast.makeText(this, this.resources.getString(R.string.not_imported), Toast.LENGTH_LONG).show()
            return
        }

        //get strings
        val time = formatMillisecondsTime(System.currentTimeMillis())
        val winner = mTextInput1!!.text.toString()
        val looser = mTextInput2!!.text.toString()

        //reset values of inputs
        mTextInput1!!.setText("")
        mTextInput2!!.setText("")

        //test the input
        var winnerID: String? = null
        var looserID: String? = null
        for (i in database!!.participants.indices) {
            val current = database!!.participants[i]
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
            Toast.makeText(this, this.resources.getString(R.string.entry_failed), Toast.LENGTH_LONG).show()
            return
        }

        //log an entry
        database!!.addEntry(LogEntry(time, winnerID, looserID))
        Toast.makeText(this, this.resources.getString(R.string.entry_added), Toast.LENGTH_LONG).show()

        //change focus
        mTextInput1!!.isFocusableInTouchMode = true
        mTextInput1!!.requestFocus()
    }

    private fun formatMillisecondsTime(millis: Long): String {
        @SuppressLint("SimpleDateFormat") val simpleDateFormat = SimpleDateFormat("HH:mm:ss")
        val date = Date(millis)
        return simpleDateFormat.format(date)
    }
}