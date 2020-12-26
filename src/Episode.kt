import java.io.Serializable
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors


class Episode(val series: String, val episode: String) : Serializable {

    private val songsTreeMap: TreeMap<ClosedRange<Time>, Song> = TreeMap(
            compareBy<ClosedRange<Time>> { it.start }.thenBy { it.endInclusive }
    )

    fun addSong(range: ClosedRange<Time>, song: Song) {
        songsTreeMap[range] = song
    }

    fun getSong(minute: Int, second: Int) = getSong(Time(minute, second))
    fun getSong(time: Time): Song? {
        val range = songsTreeMap.floorEntry(time..time)
                ?: songsTreeMap.floorEntry(time + 1..time + 1)
                ?: return null
        return if (range.key.contains(time)) range.value else null
    }

    fun printSongs() {
        for (pair in songsTreeMap) {
            println(pair)
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

    data class Id(val series: String, val episode: String) {
        companion object {
            fun parse(text: String): Id {
                val titleMatches = Regex(
                        "\\[(?<series>\\d{2})x(?<episode>\\d{2}[ab]?)\\] " +
                                ".*"
//                    "[A-Z][a-z]+ \\d{1,2}(st|nd|rd|th), 20[01]\\d( \n\\[[a-zA-Z\\d \\.]+\\])?"
                ).find(text)?.groups ?: throw IllegalArgumentException("Bad title text")

                val seriesNum = titleMatches["series"]?.value
                        ?: throw IllegalArgumentException("Series not found in title")
                val episodeNum = titleMatches["episode"]?.value
                        ?: throw IllegalArgumentException("Episode not found in title")

                val dFormatter = DecimalFormat("00")
                return Id(seriesNum, episodeNum)
            }
        }
    }

    companion object {

        fun parse(seriesNum: String, episodeNum: String, body: String): Episode {
            val episode = Episode(seriesNum, episodeNum)

            val bodyMatches =
                    Pattern.compile(
                            """(?<begin>[0-9]{2}:[0-9]{2}) - (?<end>[0-9]{2}:[0-9]{2}) - (?<name>.+)"""
                    )
                            .matcher(body)
                            .results()
                            .map { it.group() }
                            .collect(Collectors.toList())

            for (match in bodyMatches) {
                val delimited = match.split(" - ")
                val range = Time.parse(delimited[0])..Time.parse(delimited[1])
                if (delimited.size == 3) episode.addSong(range, Song(null, null))
                else episode.addSong(range, Song(delimited[3], delimited[2]))
            }

            return episode
        }
    }

}

