import java.io.Serializable

class Time private constructor(private val value: Int) : Comparable<Time>, Serializable {

    constructor(minutes: Int, seconds: Int) : this(minutes * 60 + seconds)

    val minutes
        get() = value / 60
    val seconds
        get() = value % 60

    override fun compareTo(other: Time) = value.compareTo(value)

    operator fun inc(): Time = Time(value + 1)

    operator fun dec(): Time = Time(value - 1)

    operator fun plus(other: Time) = Time(value + other.value)

    operator fun plus(seconds: Int) = Time(value + seconds)

    operator fun minus(other: Time) = Time(value - other.value)

    operator fun minus(seconds: Int) = Time(value - seconds)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Time) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int = value

    override fun toString(): String {
        return "Time(minutes=$minutes,seconds=$seconds)"
    }

    companion object {
        fun parse(timeStr: String): Time {
            val split = timeStr.replace(" ", "").split(":")
            return Time(Integer.parseInt(split[0]), Integer.parseInt(split[1]))
        }
    }


}
