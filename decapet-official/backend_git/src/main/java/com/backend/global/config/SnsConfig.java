package com.backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.SnsClientBuilder;

@Configuration
public class SnsConfig {

	@Value("${aws.sns.region}")
	private String region;

	@Value("${aws.access-key:}")
	private String accessKey;

	@Value("${aws.secret-key:}")
	private String secretKey;

	@Bean
	public SnsClient snsClient() {
		SnsClientBuilder builder = SnsClient.builder()
			.region(Region.of(region));

		if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
			builder.credentialsProvider(
				StaticCredentialsProvider.create(
					AwsBasicCredentials.create(accessKey, secretKey)
				)
			);
		}

		return builder.build();
	}
}
