package global.cursor

data class Cursor(
    val next: String?,
) {
    companion object {
        fun empty() = global.cursor.Cursor(next = null)
    }
}
