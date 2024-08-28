package config

import note.composeapp.generated.resources.Res
import note.composeapp.generated.resources.dining
import note.composeapp.generated.resources.gaming
import note.composeapp.generated.resources.recreation
import note.composeapp.generated.resources.shopping
import org.jetbrains.compose.resources.StringResource


val stringMap = mutableMapOf<String, StringResource>(
    "shopping" to Res.string.shopping,
    "dining" to Res.string.dining,
    "recreation" to Res.string.recreation,
    "gaming" to Res.string.gaming,
)

fun getStringResource(key: String): StringResource {
    return stringMap.getOrElse(key) { Res.string.shopping }
}