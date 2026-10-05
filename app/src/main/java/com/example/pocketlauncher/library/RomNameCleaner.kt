package com.example.pocketlauncher.library

object RomNameCleaner {

    private val leadingReleaseNumber =
        Regex("""^\s*\d{1,5}\s*[-._]\s*""")

    private val metadataGroup =
        Regex("""\s*[\[(]([^\])]+)[\])]""")

    private val metadataTokens = listOf(
        "usa", "europe", "japan", "world", "australia", "korea", "china",
        "u", "e", "j", "ue", "en", "fr", "de", "es", "it",
        "rev", "revision", "beta", "proto", "prototype", "demo",
        "virtual console", "dcs", "verified", "good dump", "bad dump",
        "!", "b", "h", "t", "v"
    )

    fun clean(fileName: String): String {
        val withoutExtension = fileName.substringBeforeLast('.')
        var title = withoutExtension
            .replace(leadingReleaseNumber, "")
            .replace('_', ' ')
            .trim()

        title = metadataGroup.replace(title) { match ->
            val value = match.groupValues[1].trim().lowercase()
            if (looksLikeMetadata(value)) "" else match.value
        }

        return title
            .replace(Regex("""\s{2,}"""), " ")
            .replace(Regex("""\s+-\s+$"""), "")
            .trim()
    }

    private fun looksLikeMetadata(value: String): Boolean {
        if (value.matches(Regex("""[a-z]?\d+"""))) return true

        return metadataTokens.any { token ->
            value == token ||
                value.startsWith("${token} ") ||
                value.contains(", ${token}") ||
                value.contains("${token},") ||
                value.contains(" ${token} ")
        }
    }
}
