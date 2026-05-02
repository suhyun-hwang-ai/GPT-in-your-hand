package gpt.`in`.your.hand.tarot

import android.content.Context
import org.json.JSONObject
import kotlin.random.Random

/**
 * 책 19장 — 타로 카드 데이터 로드, 무작위 카드 뽑기, 사용자 메시지 합성.
 *
 * 카드 데이터: app/src/main/assets/data/tarot_cards.json
 */
class TarotPromptBuilder(
    context: Context,
    private val random: Random = Random.Default,
) {

    private val cards: List<TarotCard> = loadCards(context)

    private fun loadCards(context: Context): List<TarotCard> = try {
        context.assets.open("data/tarot_cards.json").use { input ->
            val raw = input.reader().readText()
            val obj = JSONObject(raw)
            val arr = obj.getJSONArray("cards")
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val keywords = o.getJSONArray("keywords").let { ja ->
                        buildList { for (j in 0 until ja.length()) add(ja.getString(j)) }
                    }
                    add(
                        TarotCard(
                            id = o.getString("id"),
                            nameKr = o.getString("name_kr"),
                            nameEn = o.getString("name_en"),
                            arcana = o.getString("arcana"),
                            keywords = keywords,
                        )
                    )
                }
            }
        }
    } catch (t: Throwable) {
        emptyList()
    }

    fun drawRandomCard(): TarotCard? = cards.takeIf { it.isNotEmpty() }?.random(random)

    /**
     * 사용자 턴 메시지 합성: 질문 + 뽑힌 카드 정보. assistant는 이걸 보고 해석 응답.
     */
    fun formatUserTurn(question: String, card: TarotCard): String = buildString {
        append("[질문] ")
        append(question.ifBlank { "이 카드의 의미를 풀어 주세요." })
        append('\n')
        append("[뽑힌 카드] ")
        append(card.nameKr).append(" (").append(card.nameEn).append(")")
        append('\n')
        append("[키워드] ")
        append(card.keywords.joinToString(", "))
    }
}
