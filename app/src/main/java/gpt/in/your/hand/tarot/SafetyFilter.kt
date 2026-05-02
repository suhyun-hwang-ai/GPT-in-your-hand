package gpt.`in`.your.hand.tarot

/**
 * 책 19.4 거절·안전 처리.
 *
 * 의료·법률·금융 등 단정적 자문이 필요한 도메인 키워드 감지 시
 * 본 필터가 사전 안내 메시지를 반환한다. 모델은 이 메시지를 본문에 인용하거나
 * 자체적인 안전 응답을 생성할 수 있다.
 */
object SafetyFilter {

    private val medicalKeywords = listOf("진단", "치료", "약", "수술", "병", "질환")
    private val legalKeywords = listOf("소송", "고소", "변호사", "계약", "위법")
    private val financialKeywords = listOf("투자", "주식", "코인", "베팅", "도박")
    private val selfHarmKeywords = listOf("자해", "자살", "죽고", "해치")

    fun classify(userText: String): SafetyCategory {
        val text = userText.replace(" ", "")
        return when {
            selfHarmKeywords.any { it in text } -> SafetyCategory.SELF_HARM
            medicalKeywords.any { it in text } -> SafetyCategory.MEDICAL
            legalKeywords.any { it in text } -> SafetyCategory.LEGAL
            financialKeywords.any { it in text } -> SafetyCategory.FINANCIAL
            else -> SafetyCategory.OK
        }
    }

    fun guidanceFor(category: SafetyCategory): String? = when (category) {
        SafetyCategory.OK -> null
        SafetyCategory.SELF_HARM ->
            "이 주제는 타로 해석으로 답할 수 없는 영역입니다. 가까운 정신건강 전문가나 1393 자살예방상담전화에 도움을 받으시길 권합니다."
        SafetyCategory.MEDICAL ->
            "의료적 판단은 의사·약사 등 전문가의 진료를 받으시는 것이 안전합니다. 타로는 마음의 결을 짚는 도구로만 활용해 주세요."
        SafetyCategory.LEGAL ->
            "법률적 문제는 변호사 등 전문가 상담을 권합니다. 타로 해석은 결정의 참고로만 활용해 주세요."
        SafetyCategory.FINANCIAL ->
            "투자·금융 결정은 본인 책임이며, 자격 있는 전문가의 자문을 받으시길 권합니다. 타로는 직접적인 투자 신호가 아닙니다."
    }
}

enum class SafetyCategory {
    OK,
    SELF_HARM,
    MEDICAL,
    LEGAL,
    FINANCIAL,
}
