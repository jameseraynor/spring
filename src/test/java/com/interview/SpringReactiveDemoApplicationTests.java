package com.interview;

import com.interview.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.r2dbc.url=r2dbc:h2:mem:///testdb-integration",
    "spring.sql.init.mode=always"
})
class SpringReactiveDemoApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void contextLoads() {
        // This test ensures that the Spring context loads successfully
    }

    @Test
    void getAllUsers_IntegrationTest() {
        webTestClient.get()
                .uri("/api/users")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(User.class)
                .hasSize(5); // Based on our data.sql
    }

    @Test
    void createAndRetrieveUser_IntegrationTest() {
        User newUser = new User("Integration Test User", "integration@test.com", "QA");

        // Create user
        webTestClient.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newUser)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(User.class)
                .value(user -> {
                    assert user.getId() != null;
                    assert user.getName().equals("Integration Test User");
                    assert user.getEmail().equals("integration@test.com");
                    assert user.getDepartment().equals("QA");
                });
    }

    @Test
    void functionalEndpoints_IntegrationTest() {
        // Test functional routing
        webTestClient.get()
                .uri("/api/functional/users")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(User.class)
                .hasSize(5); // All 5 users from data.sql
    }

    @Test
    void getUsersByDepartment_IntegrationTest() {
        webTestClient.get()
                .uri("/api/users/department/Engineering")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(User.class)
                .hasSize(2); // Based on our data.sql
    }

    @Test
    void searchUsers_IntegrationTest() {
        webTestClient.get()
                .uri("/api/users/search?name=John")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(User.class)
                .hasSize(2); // John Doe and Bob Johnson
    }

    @Test
    void getUserCount_IntegrationTest() {
        webTestClient.get()
                .uri("/api/users/department/Engineering/count")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .isEqualTo(1L);
    }

    @Test
    void getUserEmails_IntegrationTest() {
        webTestClient.get()
                .uri("/api/users/department/Engineering/emails")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(String.class)
                .hasSize(1)
                .contains("bob.johnson@company.com");
    }

    @Test
    void updateUser_IntegrationTest() {
        User updateData = new User("John Updated", "john.updated@company.com", "Marketing");

        webTestClient.put()
                .uri("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateData)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .value(user -> {
                    assert user.getId().equals(1L);
                    assert user.getName().equals("John Updated");
                    assert user.getEmail().equals("john.updated@company.com");
                    assert user.getDepartment().equals("Marketing");
                });
    }

    @Test
    void deleteUser_IntegrationTest() {
        // First create a user that we can safely delete
        User userToDelete = new User("Delete Me", "delete@example.com", "Temp");
        
        // Create the user
        webTestClient.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userToDelete)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(User.class)
                .value(user -> {
                    // Delete the created user
                    webTestClient.delete()
                            .uri("/api/users/" + user.getId())
                            .exchange()
                            .expectStatus().isNoContent();

                    // Verify user is deleted
                    webTestClient.get()
                            .uri("/api/users/" + user.getId())
                            .exchange()
                            .expectStatus().isNotFound();
                });
    }

    @Test
    void invalidUserCreation_ShouldReturn400() {
        User invalidUser = new User("", "invalid-email", "Marketing");

        webTestClient.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidUser)
                .exchange()
                .expectStatus().isBadRequest();
    }
}