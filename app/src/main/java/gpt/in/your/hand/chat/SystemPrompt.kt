package gpt.`in`.your.hand.chat

import android.content.Context

/**
 * 시스템 프롬프트 로더. assets/prompts/{name}.txt 에서 읽는다.
 */
object SystemPrompt {
    fun load(context: Context, name: String): String {
        return context.assets.open("prompts/$name.txt").use { it.reader().readText() }
    }
}
