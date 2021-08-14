package sk.vyzyvacky.fragments

import sk.vyzyvacky.R
import sk.vyzyvacky.model.Participant

class CompetitorsFragment : TableFragment() {

    override fun getHeaderStrings(): Array<String?> {
        return resources.getStringArray(R.array.competitors_header)
    }

    override fun getDataObjects(): ArrayList<Any> {
        @Suppress("UNCHECKED_CAST")
        return dataHandler.getParticipants() as ArrayList<Any>
    }

    override fun getStringsFromObject(i: Int, current: Any): Array<String?> {
        val strings = arrayOfNulls<String>(4)
        val participantObject = current as Participant

        strings[0] = participantObject.id.toString()
        strings[1] = participantObject.firstname
        strings[2] = participantObject.lastname
        strings[3] = participantObject.team
        return strings
    }

    override fun getErrorString(): String {
        return resources.getString(R.string.competitors_error)
    }
}