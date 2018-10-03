import java.io.*

data class Episode(val series: Int, val episode: Int) : AbstractEpisode(series) {

    companion object {
        fun write(episode: Episode) = write(File(formatString(episode.series, episode.episode)), episode)

        fun write(file: File, episode: Episode) {
            val output = ObjectOutputStream(FileOutputStream(file))
            output.writeObject(episode)
            output.close()
        }

        fun read(series: Int, episode: Int) = read(File(formatString(series, episode)))

        fun read(file: File) = AbstractEpisode.read(file) as Episode

        /*private */fun formatString(series: Int, episode: Int) = "${AbstractEpisode
                .basePath}S${series}E${episode}.tgk"
    }
}