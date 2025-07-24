package com.interview.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class NotificationService {
    
    public Mono<String> sendWelcomeNotification(String email) {
        return Mono.fromCallable(() -> {
            // Simulate async notification sending
            System.out.println("Sending welcome notification to: " + email);
            return "Notification sent to " + email;
        })
        .delayElement(Duration.ofMillis(500))
        .doOnSuccess(result -> System.out.println("Notification completed: " + result))
        .doOnError(error -> System.err.println("Notification failed: " + error.getMessage()));
    }
    
    public Mono<Void> sendBulkNotifications(java.util.List<String> emails) {
        return reactor.core.publisher.Flux.fromIterable(emails)
                .flatMap(this::sendWelcomeNotification)
                .then();
    }
}