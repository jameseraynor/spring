package com.interview.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void sendWelcomeNotification_ShouldReturnSuccessMessage() {
        // Given
        String email = "test@example.com";

        // When & Then
        StepVerifier.create(notificationService.sendWelcomeNotification(email))
                .expectNext("Notification sent to test@example.com")
                .verifyComplete();
    }

    @Test
    void sendBulkNotifications_ShouldCompleteSuccessfully() {
        // Given
        List<String> emails = Arrays.asList(
                "user1@example.com",
                "user2@example.com",
                "user3@example.com"
        );

        // When & Then
        StepVerifier.create(notificationService.sendBulkNotifications(emails))
                .verifyComplete();
    }

    @Test
    void sendBulkNotifications_ShouldHandleEmptyList() {
        // Given
        List<String> emails = Arrays.asList();

        // When & Then
        StepVerifier.create(notificationService.sendBulkNotifications(emails))
                .verifyComplete();
    }
}