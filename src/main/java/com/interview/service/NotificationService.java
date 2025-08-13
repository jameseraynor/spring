package com.interview.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
/**
 * Asynchronous notification operations using Project Reactor.
 *
 * Simulates sending notifications in a non-blocking way. In a real system this
 * would integrate with an email/SMS/push provider.
 */
public class NotificationService {
    
    /**
     * Sends a single welcome notification to the given email.
     *
     * The operation is simulated via a delayed Mono to emulate async I/O. Logs are
     * emitted on success or failure for observability.
     */
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
    
    /**
     * Sends notifications to a collection of email addresses.
     *
     * Returns a completion signal when all notifications have been processed.
     */
    public Mono<Void> sendBulkNotifications(java.util.List<String> emails) {
        return reactor.core.publisher.Flux.fromIterable(emails)
                .flatMap(this::sendWelcomeNotification)
                .then();
    }
}