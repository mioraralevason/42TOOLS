package com.ecole._2.services;

import com.ecole._2.models.User_;
import com.ecole._2.repositories.User_Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserService {

    private final User_Repository userRepository;

    // Constructor (no return type)
    public UserService(User_Repository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a new user after validating required fields.
     *
     * @param user The user to create
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
    public void createUser(User_ user) {
        validateUser(user);
        userRepository.createUser_(user);
    }

    /**
     * Retrieves all users.
     *
     * @return List of all users
     */
    public List<User_> getAllUsers() {
        return userRepository.getAllUser_s();
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId The ID of the user to retrieve
     * @return The user, or null if not found
     * @throws IllegalArgumentException if userId is empty or null
     */
    public User_ getUserById(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("userId must not be empty");
        }
        User_ user = userRepository.getUser_ById(userId);
        if (user == null) {
            throw new IllegalStateException("User with ID " + userId + " not found");
        }
        return user;
    }

    /**
     * Updates an existing user after validating required fields.
     *
     * @param user The user with updated data
     * @throws IllegalArgumentException if required fields are missing or invalid
     * @throws IllegalStateException if the user is not found
     */
    public void updateUser(User_ user) {
        validateUser(user);
        userRepository.updateUser_(user);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param userId The ID of the user to delete
     * @throws IllegalArgumentException if userId is empty or null
     * @throws IllegalStateException if the user is not found
     */
    public void deleteUser(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("userId must not be empty");
        }
        userRepository.deleteUser_(userId);
    }

    /**
     * Validates required fields for a user.
     *
     * @param user The user to validate
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
    private void validateUser(User_ user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        if (!StringUtils.hasText(user.getUserId())) {
            throw new IllegalArgumentException("userId must not be empty");
        }
        if (!StringUtils.hasText(user.getEmail())) {
            throw new IllegalArgumentException("email must not be empty");
        }
        if (!StringUtils.hasText(user.getLogin())) {
            throw new IllegalArgumentException("login must not be empty");
        }
        if (!StringUtils.hasText(user.getDisplayname())) {
            throw new IllegalArgumentException("displayname must not be empty");
        }
        if (user.getImageId() <= 0) {
            throw new IllegalArgumentException("imageId must be a valid positive integer");
        }
    }
}