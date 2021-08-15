package sk.vyzyvacky.utilities

import android.content.Context
import android.widget.Toast
import com.android.volley.Request
import org.json.JSONArray
import org.json.JSONObject
import sk.vyzyvacky.R
import sk.vyzyvacky.model.Game
import sk.vyzyvacky.model.LogEntry
import sk.vyzyvacky.model.Participant

class DataHandler(context: Context) {
    private val PREF_GAME: String = "game"
    private val PREF_PARTICIPANTS: String = "participants"
    private val PREF_ENTRIES: String = "entries"

    private var tinyDB = TinyDB(context)
    private val ctx = context

    fun getToken(): String {
        return if (getGame() != null) getGame()!!.token else ""
    }

    fun setToken(token: String) {
        val game = getGame() ?: return
        game.token = token
        setGame(game)
    }

    fun setGame(game: Game) {
        tinyDB.putObject(PREF_GAME, game as Any)
    }

    fun getGame(): Game? {
        return try {
            tinyDB.getObject(PREF_GAME, Game::class.java)
        } catch (e: NullPointerException) {
            null
        }
    }

    fun removeGame() {
        tinyDB.remove(PREF_GAME)
    }

    fun importParticipants() {
        HttpRequestManager.sendArrayRequest(
            ctx, null, RequestType.PARTICIPANT, Request.Method.GET,
            { response: JSONArray, success: Boolean ->
                if (success) {
                    val participantArr = ArrayList<Participant>()
                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)

                        val firstname = obj.get("firstName") as String
                        val lastname = obj.get("lastName") as String
                        val id = obj.get("id") as Int
                        val team = obj.get("name") as String
                        val color = obj.get("color") as String

                        participantArr.add(Participant(id, firstname, lastname, team, color))
                    }
                    setParticipants(participantArr)
                    Toast.makeText(
                        ctx,
                        ctx.resources.getString(R.string.import_ok),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    println("Error: $response")
                    Toast.makeText(
                        ctx,
                        ctx.resources.getString(R.string.import_ko),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
        )
    }

    fun exportEntries() {
        val log = getEntries()
        val entriesToSend: ArrayList<LogEntry> = ArrayList()
        val jsonArr = JSONArray()

        //write entries into JSONArray
        log.forEach { entry ->
            if (!entry.sent) {
                val obj = JSONObject()
                obj.put("time", entry.time)
                obj.put("winner", entry.winner)
                obj.put("looser", entry.looser)

                entriesToSend.add(entry)
                jsonArr.put(obj)
            }
        }

        //send
        HttpRequestManager.sendArrayRequest(ctx, jsonArr, RequestType.ENTRY, Request.Method.POST,
            { response: JSONArray, success: Boolean ->
                if (success) {
                    //delete mark entries as sent
                    markEntriesAsSent(entriesToSend)
                    Toast.makeText(
                        ctx,
                        ctx.resources.getString(R.string.export_ok),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    //print error message and carry on
                    println("Error: $response")
                    Toast.makeText(
                        ctx,
                        ctx.resources.getString(R.string.export_ko),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun setParticipants(arr: ArrayList<Participant>) {
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

    private fun markEntriesAsSent(sent: ArrayList<LogEntry>) {
        if (sent.size == 0)
            return

        val entries = getEntries()
        entries.forEach { entry ->
            sent.forEach {
                if (entry.time == it.time) {
                    entry.sent = true
                }
            }
        }
        setEntries(entries)
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