package gpt.`in`.your.hand.tarot

data class TarotCard(
    val id: String,
    val nameKr: String,
    val nameEn: String,
    val arcana: String,
    val keywords: List<String>,
)
