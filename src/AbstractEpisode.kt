import java.io.*
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors
import kotlin.collections.HashMap

abstract class AbstractEpisode(series: Int) : Serializable {

    private val songs: MutableMap<Time, Song> = HashMap()

    fun addSong(range: ClosedRange<Time>, song: Song) {
        for (time in range) {
            songs[time] = song
        }
    }

    fun getSong(minute: Int, second: Int) = getSong(Time(minute, second))
    fun getSong(time: Time): Song? = songs[time]

    fun printSongs() {
        if (songs.isEmpty()) {
            return
        }

        val songsTree: TreeMap<Time, Song> = TreeMap(songs)
        val rangeMap: MutableMap<ClosedRange<Time>, Song> = TreeMap()
        var curRange: ClosedRange<Time> = songsTree.firstKey()..songsTree.firstKey()
        var curSong: Song = songsTree.firstEntry().value

        for (pair in songsTree) {
            if (pair.key <= curRange.endInclusive + 1)
                curRange = curRange.start..pair.key
            else {
                rangeMap[curRange] = curSong
                curRange = pair.key..pair.key
                curSong = pair.value
            }
        }

        print(rangeMap)
    }

    fun parseFile(fileName: String) = parseFile(File(fileName))
    fun parseFile(file: File) {
        if (!file.exists()) {
            println("No file called ${file.name}")
        }
        parseString(file.readText())
    }

    fun parseString(str: String) {
        val matches =
                Pattern.compile("""[0-9]{2}:[0-9]{2} - [0-9]{2}:[0-9]{2} - (\? MP3 Sample|.+? - .+)""")
                        .matcher(str)
                        .results()
                        .map { it.group() }
                        .collect(Collectors.toList())

        for (match in matches) {
            val delimited = match.split(" - ")
            val range = Time.parse(delimited[0])..Time.parse(delimited[1])
            if (delimited.size == 3) addSong(range, Song(null, null))
            else addSong(range, Song(delimited[2], delimited[3]))
        }
    }

    private operator fun ClosedRange<Time>.iterator(): Iterator<Time> {
        val range = this
        return object : Iterator<Time> {
            var cur = range.start
            override fun hasNext(): Boolean = cur in range
            override fun next(): Time = cur++
        }
    }

    companion object {
        val basePath = (System.getProperty("user.home") ?: File(".").canonicalPath) +
                "${File.separatorChar}.tgk${File.separatorChar}"

        fun write(file: File, abstractEpisode: AbstractEpisode) {
            val output = ObjectOutputStream(FileOutputStream(file))
            output.writeObject(abstractEpisode)
            output.close()
        }

        fun read(file: File): AbstractEpisode {
//            if (!file.exists()) throw IOE
            val input = ObjectInputStream(FileInputStream(file))
            return input.readObject() as AbstractEpisode
        }
    }
}
