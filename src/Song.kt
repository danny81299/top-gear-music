import java.io.Serializable
import java.lang.IllegalArgumentException

data class Song(val name: String?, val artist: String?, val album: String? = null): Serializable {
    companion object {
        fun parse(input: String): Song {
            val delimited = input.split(" - ")
            val range = Time.parse(delimited[0])..Time.parse(delimited[1])
            return when (delimited.size) {
                3 -> Song(null, null)
                4 -> Song(delimited[2], delimited[3])
                else -> throw IllegalArgumentException("Bad input")
            }
        }
    }
}