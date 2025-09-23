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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public void createUser(@RequestBody User_ user, HttpSession session) {
        var userResponse = (com.ecole._2.models.User) session.getAttribute("userResponse");
        if (userResponse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        String kind = (String) session.getAttribute("kind");
        if (!"admin".equalsIgnoreCase(kind)) {
            logger.warn("Unauthorized attempt to create user by non-admin user");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can create users");
        }
        userService.createUser(user);
    }

    @GetMapping
    public List<User_> getAllUsers(HttpSession session) {
        var userResponse = (com.ecole._2.models.User) session.getAttribute("userResponse");
        if (userResponse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        String kind = (String) session.getAttribute("kind");
        if (!"admin".equalsIgnoreCase(kind)) {
            logger.warn("Unauthorized attempt to retrieve all users by non-admin user");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can retrieve all users");
        }
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public User_ getUserById(@PathVariable String userId, HttpSession session) {
        var userResponse = (com.ecole._2.models.User) session.getAttribute("userResponse");
        if (userResponse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        String kind = (String) session.getAttribute("kind");

        if (!"admin".equalsIgnoreCase(kind)) {
            String sessionUserId = userResponse != null ? userResponse.getId() : null;
            if (!userId.equals(sessionUserId)) {
                logger.warn("Non-admin user attempted to access user ID: {}", userId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own user data");
            }
        }

        return userService.getUserById(userId);
    }

    @PutMapping("/{userId}")
    public void updateUser(@PathVariable String userId, @RequestBody User_ user, HttpSession session) {
        var userResponse = (com.ecole._2.models.User) session.getAttribute("userResponse");
        if (userResponse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        String kind = (String) session.getAttribute("kind");

        if (!"admin".equalsIgnoreCase(kind)) {
            String sessionUserId = userResponse != null ? userResponse.getId() : null;
            if (!userId.equals(sessionUserId) || !userId.equals(user.getUserId())) {
                logger.warn("Non-admin user attempted to update user ID: {}", userId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own user data");
            }
        }

        user.setUserId(userId);
        userService.updateUser(user);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable String userId, HttpSession session) {
        var userResponse = (com.ecole._2.models.User) session.getAttribute("userResponse");
        if (userResponse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        String kind = (String) session.getAttribute("kind");
        if (!"admin".equalsIgnoreCase(kind)) {
            logger.warn("Unauthorized attempt to delete user ID: {} by non-admin user", userId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can delete users");
        }
        userService.deleteUser(userId);
    }
}