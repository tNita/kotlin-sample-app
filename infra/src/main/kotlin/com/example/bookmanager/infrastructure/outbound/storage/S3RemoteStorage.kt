package com.example.bookmanager.infrastructure.outbound.storage

import com.example.bookmanager.application.port.outbound.RemoteInput
import com.example.bookmanager.application.port.outbound.RemoteOutput
import com.example.bookmanager.application.port.outbound.RemoteStorage
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@Component
class S3RemoteStorage(
    private val s3Client: S3Client,
    private val objectMapper: ObjectMapper,
) : RemoteStorage {
    override fun openInput(path: String): RemoteInput {
        val location = S3Location.parse(path)
        return S3Input(s3Client.getObject(GetObjectRequest.builder().bucket(location.bucket).key(location.key).build()))
    }

    override fun openOutput(path: String): RemoteOutput = S3Output(path)

    private inner class S3Input(private val input: InputStream) : RemoteInput {
        override fun <T : Any> lines(dataType: Class<T>): Sequence<T> =
            input.bufferedReader(StandardCharsets.UTF_8).lineSequence()
                .filter(String::isNotBlank)
                .map { objectMapper.readValue(it, dataType) }
        override fun close() = input.close()
    }

    private inner class S3Output(private val path: String) : RemoteOutput {
        private val temporaryFile = Files.createTempFile("remote-storage-", ".jsonl")
        private val writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)

        override fun write(value: Any) {
            writer.write(objectMapper.writeValueAsString(value))
            writer.newLine()
        }

        override fun complete() {
            writer.flush()
            val location = S3Location.parse(path)
            s3Client.putObject(
                PutObjectRequest.builder().bucket(location.bucket).key(location.key).contentType("application/x-ndjson").build(),
                RequestBody.fromFile(temporaryFile),
            )
        }

        override fun close() {
            try {
                writer.close()
            } finally {
                Files.deleteIfExists(temporaryFile)
            }
        }
    }

    private data class S3Location(val bucket: String, val key: String) {
        companion object {
            fun parse(path: String): S3Location {
                require(path.startsWith("s3://")) { "S3パスは s3://bucket/key 形式で指定してください: $path" }
                val separator = path.indexOf('/', "s3://".length)
                require(separator > "s3://".length && separator < path.lastIndex) { "S3パスはバケット名とキーを含めて指定してください: $path" }
                return S3Location(path.substring("s3://".length, separator), path.substring(separator + 1))
            }
        }
    }
}
