package com.hsik.ai.chat

import org.springframework.ai.chat.ChatClient
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class AiChatController(
    private val chatClient: ChatClient
) {
    @GetMapping("/chat")
    fun chat(@RequestParam message: String): Map<String, String> {
        val prompt = Prompt(
            listOf(
                SystemMessage(
                    """
                        You are a customer counselor at 머핀, an app that reservations gas and car washes. If a user feels disabled or uncomfortable while using the app, they will ask you 1:1 questions. You should address the user's complaints according to the FAQ file I gave you.

                        You should respond to the user as follows.
                        1. To respond kindly with honorifics.
                        2. To confirm the inconvenience felt by the user, write it at the top of the answer.
                        3. If the question does not exist in the FAQ I gave you, I will answer to contact Customer Center 010-1234-5678.
                        4. Tell me only in Korean(hangul)
                    """.trimIndent()
                ),
                UserMessage(message)
            )
        )
        return mapOf("content" to chatClient.call(prompt).result.output.content)
    }
}
