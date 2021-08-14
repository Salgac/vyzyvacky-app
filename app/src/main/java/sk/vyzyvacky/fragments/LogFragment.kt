package sk.vyzyvacky.fragments

import sk.vyzyvacky.R
import sk.vyzyvacky.model.LogEntry

class LogFragment : TableFragment() {

    override fun init() {
        headerArray = resources.getStringArray(R.array.log_header)
        data = dataHandler.getEntries() as ArrayList<Any>
        toastString = resources.getString(R.string.log_error)
    }

    override fun getData(i: Int, current: Any): Array<String?> {
        val strings = arrayOfNulls<String>(4)
        val logObject = current as LogEntry

        strings[0] = i.toString()
        strings[1] = logObject.time
        strings[2] = dataHandler.getFullParticipantNameByID(logObject.winner)
        strings[3] = dataHandler.getFullParticipantNameByID(logObject.looser)
        return strings
    }
}