package com.interview.repository;

import com.interview.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.util.UUID;

@DataR2dbcTest
@TestPropertySource(properties = {
    "spring.r2dbc.url=r2dbc:h2:mem:///testdb-repo",
    "spring.sql.init.mode=always"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Use a unique email to avoid conflicts
        String uniqueEmail = "test" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        testUser = new User("Test User", uniqueEmail, "Testing");
    }

    @Test
    void save_ShouldPersistUser() {
        StepVerifier.create(userRepository.save(testUser))
                .expectNextMatches(user -> {
                    assert user.getId() != null;
                    assert user.getName().equals("Test User");
                    assert user.getEmail().equals(testUser.getEmail());
                    assert user.getDepartment().equals("Testing");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void findById_ShouldReturnUser() {
        StepVerifier.create(userRepository.save(testUser)
                .flatMap(savedUser -> userRepository.findById(savedUser.getId())))
                .expectNextMatches(user -> user.getName().equals("Test User"))
                .verifyComplete();
    }

    @Test
    void findByDepartment_ShouldReturnUsersInDepartment() {
        StepVerifier.create(userRepository.findByDepartment("Engineering")
                .take(1)) // Take only the first element
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByEmail_ShouldReturnUser() {
        StepVerifier.create(userRepository.findByEmail("john.doe@company.com"))
                .expectNextMatches(user -> user.getName().equals("John Doe"))
                .verifyComplete();
    }

    @Test
    void findByNamePattern_ShouldReturnMatchingUsers() {
        StepVerifier.create(userRepository.findByNamePattern("%John%")
                .take(1)) // Take only the first element
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void countByDepartment_ShouldReturnCorrectCount() {
        StepVerifier.create(userRepository.countByDepartment("Engineering"))
                .expectNext(2L) // There are 2 users in Engineering department
                .verifyComplete();
    }

    @Test
    void deleteById_ShouldRemoveUser() {
        StepVerifier.create(userRepository.save(testUser)
                .flatMap(savedUser -> {
                    Long id = savedUser.getId();
                    return userRepository.deleteById(id)
                            .then(userRepository.findById(id));
                }))
                .verifyComplete(); // Should complete with no elements
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // First save our test user
        StepVerifier.create(userRepository.save(testUser)
                .then(userRepository.findAll().collectList()))
                .expectNextMatches(users -> users.size() >= 5) // At least 5 users (4 from data.sql + our test user)
                .verifyComplete();
    }
}