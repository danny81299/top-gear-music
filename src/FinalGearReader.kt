fun readEpisodeId(titleText: String): Pair<Int, Int>? {
    val titleMatches = Regex(
            "\\[(?<series>\\d{1,2})x(?<episode>\\d{1,2})\\] " +
                    "[A-Z][a-z]+ \\d{1,2}(st|nd|rd|th), 20[01]\\d( \n\\[[a-zA-Z\\d \\.]+\\])?"
    ).find(titleText) ?: return null

    val groups = titleMatches.groups
    val seriesNum = groups["series"]!!.value.toInt()
    val episodeNum = groups["episode"]!!.value.toInt()

    return Pair(seriesNum, episodeNum)
}

fun readSong(input: String): Song? {
    val delimited = input.split(" - ")
    val range = Time.parse(delimited[0])..Time.parse(delimited[1])
    return when (delimited.size) {
        3 -> Song(null, null)
        4 -> Song(delimited[2], delimited[3])
        else -> null
    }
}
