package config

fun isNumeric(input: String): Boolean {
    val regex = """^\d+(\.\d+)?$""".toRegex()
    return regex.matches(input)
}
