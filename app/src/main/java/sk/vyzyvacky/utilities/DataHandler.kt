package sk.vyzyvacky.utilities

import android.content.Context
import sk.vyzyvacky.model.LogEntry
import sk.vyzyvacky.model.Participant
import java.util.*

class DataHandler(context: Context) {
    private val PREF_PARTICIPANTS: String = "participants"
    private val PREF_ENTRIES: String = "entries"

    private var tinyDB = TinyDB(context)

    fun setParticipants(arr: ArrayList<Participant>) {
        val objArr: ArrayList<Any> = ArrayList()
        for (p in arr) {
            objArr.add(p as Any)
        }
        tinyDB.putListObject(PREF_PARTICIPANTS, objArr)
        System.out.println("Added: $arr")
    }

    fun getParticipants(): ArrayList<Participant> {
        val arr: ArrayList<Participant> = ArrayList()
        val objArr = tinyDB.getListObject(PREF_PARTICIPANTS, Participant::class.java)
        for (p in objArr) {
            arr.add(p as Participant)
        }
        return arr
    }

    private fun setEntries(arr: ArrayList<LogEntry>) {
        val objArr: ArrayList<Any> = ArrayList()
        for (p in arr) {
            objArr.add(p as Any)
        }
        tinyDB.putListObject(PREF_ENTRIES, objArr)
        System.out.println("Added: $arr")
    }

    fun getEntries(): ArrayList<LogEntry> {
        val arr: ArrayList<LogEntry> = ArrayList()
        val objArr = tinyDB.getListObject(PREF_ENTRIES, LogEntry::class.java)
        for (p in objArr) {
            arr.add(p as LogEntry)
        }
        return arr
    }

    fun addEntry(entry: LogEntry) {
        val entries = getEntries()
        entries.add(entry)
        setEntries(entries)
    }

    fun removeEntry(index: Int) {
        val entries = getEntries()
        entries.removeAt(index)
        setEntries(entries)
    }

    fun removeEntries() {
        tinyDB.putListObject(PREF_ENTRIES, ArrayList())
    }

    fun getNamesForAdapter(): Array<String?> {
        val participants = getParticipants()
        val names = arrayOfNulls<String>(participants.size)

        for (i in participants.indices) {
            names[i] = participants[i].lastname + " " + participants[i].firstname
        }
        return names
    }

    fun getFullParticipantNameByID(id: Int): String? {
        for (participant in getParticipants()) {
            if (participant.id == id) return participant.lastname + " " + participant.firstname
        }
        return null
    }
}