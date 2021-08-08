package sk.vyzyvacky.utilities

class Urls {
    companion object {
        private const val ROOT = "http://139.162.130.177:3000/v1"

        const val LOGIN = "$ROOT/log_in/"
        const val ENTRY = "$ROOT/entries/"
        const val PARTICIPANT = "$ROOT/people/"
    }
}