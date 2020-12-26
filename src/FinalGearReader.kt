import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.safety.Whitelist
import java.sql.Connection
import java.sql.DriverManager
import java.text.DecimalFormat

fun readFromFinalGear(url: String): Episode {
    val page = Jsoup.connect(url).get()

    val titleText: String = page.getElementsByTag("h1").first().text()
    val episodeId = Episode.Id.parse(titleText)
    val seriesNum = episodeId.series
    val episodeNum = episodeId.episode

    val bodyEl: Element = page.getElementsByClass("bbWrapper").first()
    val bodyText: String = Jsoup.clean(
            bodyEl.html(), "", Whitelist.none(), Document.OutputSettings().prettyPrint(false)
    )

    return Episode.parse(seriesNum, episodeNum, bodyText)
}

fun getText(url: String): String {
    val page = Jsoup.connect(url).get()

    val bodyEl: Element = page.getElementsByClass("bbWrapper").first()
    val bodyText: String = Jsoup.clean(
            bodyEl.html(), "", Whitelist.none(), Document.OutputSettings().prettyPrint(false)
    )

    return bodyText
}

fun getEpisodeUrls(): Map<Pair<String, String>, Map<Episode.Id, String>> {
    val base = "https://forums.finalgear.com"

    val conn: Connection = DriverManager.getConnection("jdbc:sqlite:tgm.sqlite")


    val seriesPage = Jsoup.connect("$base/forums/tg-whats-that-song.60/").get()
    val series = seriesPage.getElementsByClass("node-title").map {
        it.child(0)
    }.map {
        val series = it.html().replace(Regex("[^0-9]"), "").padStart(2, '0')
        val url = it.attr("href")!!

        val pStmt = conn.prepareStatement("INSERT OR REPLACE INTO series (series, url) VALUES (?, ?)")
        pStmt.setString(1, series)
        pStmt.setString(2, url)
        pStmt.execute()

        Pair<String, String>(series, url)
    }

    val episodes = series.map { p ->
        val series = p.first
        val url = p.second

        val episodesPage = Jsoup.connect("$base$url").get()
        p to episodesPage.getElementsByClass("structItem-title").map {
            it.child(0)
        }.map {
            val eId = Episode.Id.parse(it.html())
            val url = it.attr("href")!!
            assert(eId.series.equals(series))
            println(eId)
            val text = getText("$base$url")

            val pStmt = conn.prepareStatement(
                    "INSERT OR REPLACE INTO episodes (series, episode, url, text) VALUES (?, ?, ?, ?)"
            )
            pStmt.setString(1, eId.series)
            pStmt.setString(2, eId.episode)
            pStmt.setString(3, url)
            pStmt.setString(4, text)
            pStmt.execute()

            Pair<Episode.Id, String>(eId, url)
        } .toMap()
    } .toMap()

    return episodes
}