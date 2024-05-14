package com.hsik.ai.chat

import org.slf4j.LoggerFactory
import org.springframework.ai.document.MetadataMode
import org.springframework.ai.openai.OpenAiEmbeddingClient
import org.springframework.ai.openai.OpenAiEmbeddingOptions
import org.springframework.ai.openai.api.OpenAiApi
import org.springframework.ai.reader.JsonReader
import org.springframework.ai.retry.RetryUtils
import org.springframework.ai.vectorstore.SimpleVectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import java.io.File
import java.nio.file.Paths

@Configuration
class RagConfiguration {

    private val log = LoggerFactory.getLogger(javaClass)

    @Value("classpath:/docs/faq.json")
    private lateinit var faq: Resource

    @Value("vectorstore.json")
    private lateinit var vectorStoreName: String

    @Value("\${spring.ai.openai.api-key}")
    private lateinit var openAiApiKey: String

    @Bean
    fun openAiEmbeddingClient(): OpenAiEmbeddingClient {
        return OpenAiEmbeddingClient(
            OpenAiApi(openAiApiKey),
            MetadataMode.EMBED,
            OpenAiEmbeddingOptions.builder().withModel("text-embedding-3-large").build(),
            RetryUtils.DEFAULT_RETRY_TEMPLATE
        )
    }

    @Bean
    fun simpleVectorStore(): SimpleVectorStore {
        val simpleVectorStore = SimpleVectorStore(openAiEmbeddingClient())
        val vectorStoreFile = getVectorStoreFile()
        if (vectorStoreFile.exists()) {
            log.info("Vector Store file exists")
            simpleVectorStore.load(vectorStoreFile)
        } else {
            log.info("Vector Store file does not exist, loading documents")
            val jsonReader = JsonReader(faq, "title", "content")
            val documents = jsonReader.get()

            simpleVectorStore.add(documents)
            simpleVectorStore.save(vectorStoreFile)
        }

        return simpleVectorStore
    }

    private fun getVectorStoreFile(): File {
        val path = Paths.get("src", "main", "resources", "data")
        val absolutePath = path.toFile().absolutePath + "/" + vectorStoreName
        return File(absolutePath)
    }
}
