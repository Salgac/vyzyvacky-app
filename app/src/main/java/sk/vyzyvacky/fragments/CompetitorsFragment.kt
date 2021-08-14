package sk.vyzyvacky.fragments

import sk.vyzyvacky.R
import sk.vyzyvacky.model.Participant

class CompetitorsFragment : TableFragment() {

    override fun init() {
        headerArray = resources.getStringArray(R.array.competitors_header)
        data = dataHandler.getParticipants() as ArrayList<Any>
        toastString = resources.getString(R.string.competitors_error)
    }

    override fun getData(i: Int, current: Any): Array<String?> {
        val strings = arrayOfNulls<String>(4)
        val participantObject = current as Participant

        strings[0] = participantObject.id.toString()
        strings[1] = participantObject.firstname
        strings[2] = participantObject.lastname
        strings[3] = participantObject.team
        return strings
    }
}