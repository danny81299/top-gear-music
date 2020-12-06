import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.safety.Whitelist
import java.io.Serializable
import java.lang.RuntimeException
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors
import kotlin.collections.HashMap


class Episode(val series: Int, val episode: Int) : Serializable {

    private val songs: MutableMap<Time, Song> = HashMap()
    private val songsTreeMap: TreeMap<ClosedRange<Time>, Song> = TreeMap { r1, r2 ->
        r1.start.compareTo(r2.start)
    }

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

        val songsTree = TreeMap(songs)
        val rangeMap= TreeMap<ClosedRange<Time>, Song> { r1, r2 ->
            r1.start.compareTo(r2.start)
        }
        var curRange = songsTree.firstKey()..songsTree.firstKey()
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

       for (pair in rangeMap) {
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

    companion object {

        fun readFromFinalGear(url: String): Episode {
            val page = Jsoup.connect(url).get()

            val titleText: String = page.getElementsByTag("h1").first().text()
            val titleMatches = Regex(
                    "\\[(?<series>\\d{1,2})x(?<episode>\\d{1,2})\\] " +
                            "[A-Z][a-z]+ \\d{1,2}(st|nd|rd|th), 20[01]\\d( \n\\[[a-zA-Z\\d \\.]+\\])?"
            ).find(titleText) ?: throw RuntimeException()
            val groups = titleMatches.groups
            val seriesNum = groups["series"]!!.value.toInt()
            val episodeNum = groups["episode"]!!.value.toInt()
            val episode = Episode(seriesNum, episodeNum)

            val bodyEl: Element = page.getElementsByClass("bbWrapper").first()
            val bodyText: String = Jsoup.clean(
                    bodyEl.html(), "", Whitelist.none(), Document.OutputSettings().prettyPrint(false)
            )

            val bodyMatches =
                    Pattern.compile(
                            """(?<begin>[0-9]{2}:[0-9]{2}) - (?<end>[0-9]{2}:[0-9]{2}) - (?<name>.+)"""
                    )
                            .matcher(bodyText)
                            .results()
                            .map { it.group() }
                            .collect(Collectors.toList())

            for (match in bodyMatches) {
                val delimited = match.split(" - ")
                val range = Time.parse(delimited[0])..Time.parse(delimited[1])
                if (delimited.size == 3) episode.addSong(range, Song(null, null))
                else episode.addSong(range, Song(delimited[2], delimited[3]))
            }

            return episode
        }
    }

}

