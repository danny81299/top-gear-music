import java.io.Serializable

class ShiftedTime(minutes: Int, seconds: Int) : Comparable<ShiftedTime>, Serializable {

    val value: Int

    val minutes: Int
        get() = value shr 6
    val seconds: Int
        get() = value % 100

    init {
        if (seconds !in 0..59 || minutes < 0)
            throw IllegalArgumentException("Invalid minutes or seconds")
        value = (minutes shl 6) + seconds
    }

    override fun compareTo(other: ShiftedTime) = value.compareTo(other.value)

    operator fun inc(): ShiftedTime = when (seconds) {
        59 -> ShiftedTime(minutes + 1, 0)
        else -> ShiftedTime(minutes, seconds + 1)
    }

    operator fun dec(): ShiftedTime = when (seconds) {
        0 -> ShiftedTime(minutes - 1, 59)
        else -> ShiftedTime(minutes, seconds - 1)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ShiftedTime) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode() = value

    override fun toString() = "ShiftedTime(minutes=$minutes,seconds=$seconds)"


    companion object {
        fun parse(ShiftedTimeStr: String): ShiftedTime {
            val split = ShiftedTimeStr.replace(" ", "").split(":")
            return ShiftedTime(Integer.parseInt(split[0]), Integer.parseInt(split[1]))
        }
    }


}