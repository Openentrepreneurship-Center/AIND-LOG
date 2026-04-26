package com.backend.global.config;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

@Configuration
public class RateLimitConfig {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Bean
    public Map<String, Bucket> rateLimitBuckets() {
        return buckets;
    }

    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, this::newBucket);
    }

    private Bucket newBucket(String key) {
        // Global: 1000 requests per 5 minutes
        // Normal usage: page load triggers 10-20 API calls
        // 1000/5min = 200/min is sufficient for normal usage
        // DDoS attempts thousands/min -> blocked
        return Bucket.builder()
                .addLimit(Bandwidth.classic(1000, Refill.greedy(1000, Duration.ofMinutes(5))))
                .build();
    }

    public Bucket resolveAuthBucket(String key) {
        return buckets.computeIfAbsent("auth:" + key, k -> newAuthBucket());
    }

    private Bucket newAuthBucket() {
        // Auth endpoints (login, check-email): 30 requests per 5 minutes
        // Normal user: 1-3 login attempts per minute
        // 30/5min = 6/min is sufficient for normal usage
        // Brute force attempts hundreds/min -> blocked
        return Bucket.builder()
                .addLimit(Bandwidth.classic(30, Refill.greedy(30, Duration.ofMinutes(5))))
                .build();
    }

    public Bucket resolveSmsVerificationBucket(String key) {
        return buckets.computeIfAbsent("sms:" + key, k -> newSmsBucket());
    }

    private Bucket newSmsBucket() {
        // SMS send: 10 requests per 5 minutes
        // Normal user: 1-2 resend attempts is enough
        // SMS cost: ~20 KRW per message, unlimited = millions in damage
        // 10/5min = 2/min covers normal usage, blocks cost attacks
        return Bucket.builder()
                .addLimit(Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(5))))
                .build();
    }

    public Bucket resolveSmsVerifyBucket(String key) {
        return buckets.computeIfAbsent("sms-verify:" + key, k -> newSmsVerifyBucket());
    }

    private Bucket newSmsVerifyBucket() {
        // SMS verify: 15 requests per 5 minutes
        // 6-digit code = 1 million combinations
        // 15/5min makes brute force impossible
        // Normal user: 5 attempts max (typos)
        return Bucket.builder()
                .addLimit(Bandwidth.classic(15, Refill.greedy(15, Duration.ofMinutes(5))))
                .build();
    }

    public Bucket resolveRefreshBucket(String key) {
        return buckets.computeIfAbsent("refresh:" + key, k -> newRefreshBucket());
    }

    private Bucket newRefreshBucket() {
        // Token refresh: 5 requests per minute (stricter to prevent abuse)
        return Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1))))
                .build();
    }
}
