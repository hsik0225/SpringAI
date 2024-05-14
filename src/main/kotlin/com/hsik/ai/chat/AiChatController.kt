package com.hsik.ai.chat

import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.ollama.OllamaChatClient
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class AiChatController(
    private val chatClient: OllamaChatClient,
    private val vectorStore: VectorStore,
    @Value("classpath:/prompt/rag-prompt-template.st")
    private var ragPromptTemplate: Resource
) {
    @GetMapping("/chat")
    fun chat(
        @RequestParam message: String,
        @RequestParam type: String
    ): Map<String, String> {
        if (type == "rag") {
            val similarDocuments = vectorStore.similaritySearch(
                SearchRequest.query(message)
                    .withSimilarityThreshold(0.1)
                    .withTopK(1)
            )
            val contents = similarDocuments.map { it.content }.toList()
            val promptTemplate = PromptTemplate(ragPromptTemplate)
            val promptParameters: MutableMap<String, Any> = mutableMapOf()
            promptParameters["message"] = message
            promptParameters["documents"] = contents
            val prompt = promptTemplate.create(promptParameters)
            return mapOf("content" to chatClient.call(prompt).result.output.content)
        }

        val prompt = Prompt(
            listOf(
                SystemMessage(
                    """
                        You are a customer counselor at ChatBotApp, an app that reservations gas and car washes. If a user feels disabled or uncomfortable while using the app, they will ask you 1:1 questions. You should address the user's complaints according to the FAQ file I gave you.

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
