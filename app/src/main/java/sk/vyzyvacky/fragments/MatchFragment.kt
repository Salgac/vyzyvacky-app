package sk.vyzyvacky.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_match.*
import kotlinx.android.synthetic.main.fragment_match.view.*
import sk.vyzyvacky.R
import sk.vyzyvacky.model.LogEntry
import sk.vyzyvacky.model.SKArrayAdapter
import sk.vyzyvacky.utilities.data.DataHandler
import java.sql.Timestamp

class MatchFragment : Fragment() {
    private var layout: View? = null
    private lateinit var dataHandler: DataHandler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        layout = inflater.inflate(R.layout.fragment_match, container, false)

        //utilities
        dataHandler = DataHandler(this.requireContext())
        resetAdapter()

        //listeners
        layout!!.submit_button.setOnClickListener {
            saveLogEntry()
        }

        return layout
    }

    fun resetAdapter() {
        // Get the string array
        val nameList = dataHandler.getNamesForAdapter()

        // Create the adapter and set it to the AutoCompleteTextViews
        val adapter =
            SKArrayAdapter(this.requireContext(), android.R.layout.simple_list_item_1, nameList)
        layout!!.autoCompleteTextView1.setAdapter(adapter)
        layout!!.autoCompleteTextView2.setAdapter(adapter)
    }

    private fun saveLogEntry() {
        val participants = dataHandler.getParticipants()
        if (participants.isEmpty()) {
            Toast.makeText(this.requireContext(), "Database not imported.", Toast.LENGTH_LONG)
                .show()
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
            Toast.makeText(this.requireContext(),
                this.resources.getString(R.string.entry_failed),
                Toast.LENGTH_LONG)
                .show()
            return
        }

        //log an entry
        dataHandler.addEntry(LogEntry(time.toString(), winnerID, looserID))
        Toast.makeText(this.requireContext(),
            this.resources.getString(R.string.entry_added),
            Toast.LENGTH_LONG)
            .show()

        //change focus
        autoCompleteTextView1.isFocusableInTouchMode = true
        autoCompleteTextView1.requestFocus()
    }
}