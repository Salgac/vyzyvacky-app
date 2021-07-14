package sk.vyzyvacky.utilities

import android.content.Context
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

}