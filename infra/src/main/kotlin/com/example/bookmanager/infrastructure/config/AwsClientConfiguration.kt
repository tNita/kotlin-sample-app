package com.example.bookmanager.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sfn.SfnClient
import software.amazon.awssdk.services.sqs.SqsClient
import java.net.URI

@Configuration
class AwsClientConfiguration {
    @Bean
    fun sfnClient(properties: AwsProperties): SfnClient = SfnClient.builder().configure(properties).build()

    @Bean
    fun sqsClient(properties: AwsProperties): SqsClient = SqsClient.builder().configure(properties).build()
}

@Configuration
class AwsProperties(
    @Value("\${spring.cloud.aws.endpoint}") val endpoint: String,
    @Value("\${spring.cloud.aws.region.static}") val region: String,
    @Value("\${spring.cloud.aws.credentials.access-key}") val accessKey: String,
    @Value("\${spring.cloud.aws.credentials.secret-key}") val secretKey: String,
)

private fun <T : software.amazon.awssdk.awscore.client.builder.AwsClientBuilder<T, *>> T.configure(properties: AwsProperties): T =
    endpointOverride(URI.create(properties.endpoint))
        .region(Region.of(properties.region))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(properties.accessKey, properties.secretKey)),
        )
