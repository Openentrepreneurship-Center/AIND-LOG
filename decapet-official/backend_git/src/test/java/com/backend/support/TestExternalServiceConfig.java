package com.backend.support;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sns.SnsClient;

@TestConfiguration
public class TestExternalServiceConfig {

    @Bean
    @Primary
    public S3Client testS3Client() {
        return Mockito.mock(S3Client.class, Mockito.withSettings().lenient());
    }

    @Bean
    @Primary
    public S3Presigner testS3Presigner() {
        return Mockito.mock(S3Presigner.class, Mockito.withSettings().lenient());
    }

    @Bean
    @Primary
    public SnsClient testSnsClient() {
        return Mockito.mock(SnsClient.class, Mockito.withSettings().lenient());
    }
}
