package sk.vyzyvacky.activities

import android.widget.TableLayout
import android.widget.Toast
import sk.vyzyvacky.R

class LogActivity : TableActivity() {

    override fun init() {
        val table = findViewById<TableLayout>(R.id.table_main)
        header = arrayOf("ID", "Čas", "Víťaz", "Porazený")
        setHeader()
        //toast
        val logSize = database?.log?.size
        if (logSize == 0) Toast.makeText(this, this.resources.getString(R.string.no_entries), Toast.LENGTH_LONG).show()

        //print entries
        for (i in 0 until logSize!!) {
            val current = database?.log?.get(i)

            //get strings
            val strings = arrayOfNulls<String>(4)
            strings[0] = i.toString()
            if (current != null) {
                strings[1] = current.time
                strings[2] = database?.getFullParticipantNameByID(current.winner)
                strings[3] = database?.getFullParticipantNameByID(current.looser)
            }
            newRow(table, strings, i)
        }
    }
}