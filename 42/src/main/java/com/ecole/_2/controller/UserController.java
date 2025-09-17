package com.ecole._2.controller;

import com.ecole._2.models.User_;
import com.ecole._2.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService  userService) {
        this.userService = userService;
    }

    @PostMapping
    public void createUser(@RequestBody User_ user, HttpSession session) {
        String kind = (String) session.getAttribute("kind");
        if (!"admin".equalsIgnoreCase(kind)) {
            logger.warn("Unauthorized attempt to create user by non-admin user");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can create users");
        }
        try {
            userService.createUser(user);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input for creating user: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error processing /users POST request: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user", e);
        }
    }

    @GetMapping
    public List<User_> getAllUsers(HttpSession session) {
        // String kind = (String) session.getAttribute("kind");
        String kind = "admin"; // Simulating admin user for this example
        if (!"admin".equalsIgnoreCase(kind)) {
            logger.warn("Unauthorized attempt to retrieve all users by non-admin user");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can retrieve all users");
        }
        try {
            return userService.getAllUsers();
        } catch (Exception e) {
            logger.error("Error processing /users GET request: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve users", e);
        }
    }

    @GetMapping("/{userId}")
    public User_ getUserById(@PathVariable String userId, HttpSession session) {
        String kind = (String) session.getAttribute("kind");
        var userResponse = (com.ecole._2.models.User_) session.getAttribute("userResponse");

        if (!"admin".equalsIgnoreCase(kind)) {
            String sessionUserId = userResponse != null ? userResponse.getUserId() : null;
            if (!userId.equals(sessionUserId)) {
                logger.warn("Non-admin user attempted to access user ID: {}", userId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own user data");
            }
        }

        try {
            return userService.getUserById(userId);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid userId: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (IllegalStateException e) {
            logger.error("User not found for ID: {}", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error processing /users/{} GET request: {}", userId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve user", e);
        }
    }

    @PutMapping("/{userId}")
    public void updateUser(@PathVariable String userId, @RequestBody User_ user, HttpSession session) {
        String kind = (String) session.getAttribute("kind");
        var userResponse = (com.ecole._2.models.User_) session.getAttribute("userResponse");

        if (!"admin".equalsIgnoreCase(kind)) {
            String sessionUserId = userResponse != null ? userResponse.getUserId() : null;
            if (!userId.equals(sessionUserId) || !userId.equals(user.getUserId())) {
                logger.warn("Non-admin user attempted to update user ID: {}", userId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own user data");
            }
        }

        user.setUserId(userId);
        try {
            userService.updateUser(user);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input for updating user: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (IllegalStateException e) {
            logger.error("User not found for ID: {}", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error processing /users/{} PUT request: {}", userId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update user", e);
        }
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable String userId, HttpSession session) {
        String kind = (String) session.getAttribute("kind");
        if (!"admin".equalsIgnoreCase(kind)) {
            logger.warn("Unauthorized attempt to delete user ID: {} by non-admin user", userId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can delete users");
        }
        try {
            userService.deleteUser(userId);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid userId: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (IllegalStateException e) {
            logger.error("User not found for ID: {}", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error processing /users/{} DELETE request: {}", userId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete user", e);
        }
    }
}