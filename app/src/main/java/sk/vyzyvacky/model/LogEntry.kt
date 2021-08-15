package sk.vyzyvacky.model

class LogEntry(var time: String, var winner: Int, var looser: Int) {
    var sent: Boolean = false
}