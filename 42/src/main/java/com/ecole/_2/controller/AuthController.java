package com.ecole._2.controller;

import com.ecole._2.models.TokenResponse;
import com.ecole._2.services.User42Service;
import com.ecole._2.services.UserLocationStatsFilterService;
import com.ecole._2.services.UserLocationStatsService;
import com.ecole._2.models.User;
import com.ecole._2.models.UserLocationStat;
import com.ecole._2.services.ApiService;
import com.ecole._2.services.CampusUsersService;
import com.ecole._2.services.OAuth42Service;
import jakarta.servlet.http.HttpSession;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;

@Controller
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private OAuth42Service oauth42Service;
    
    @Autowired
    private User42Service user42Service;
    
    @Autowired
    private CampusUsersService campusUsersService;
    
    @Autowired
    private ApiService apiService;

    @Autowired
    private UserLocationStatsService userLocationStatsService;

    @Autowired
    private UserLocationStatsFilterService userLocationStatsFilterService;

    private static String CAMPUS_ID = "65";
    
    @GetMapping("/auth")
    public String auth(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            Model model,
            HttpSession session
    ) {
        logger.info("Starting authentication process with code: {}", code);

        TokenResponse tokenResponse = (TokenResponse) session.getAttribute("tokenResponse");
        User userResponse = (User) session.getAttribute("userResponse");

        // Si déjà en session, utiliser les infos
        if (tokenResponse == null || userResponse == null) {
            tokenResponse = oauth42Service.getAccessToken(code);
            if (tokenResponse == null) {
                logger.error("Failed to retrieve access token");
                model.addAttribute("error", "Authentication error");
                return "error-page";
            }

            userResponse = user42Service.getUserInfo(tokenResponse.getAccessToken());
            if (userResponse == null) {
                logger.error("Failed to retrieve user info");
                model.addAttribute("error", "Error retrieving user information");
                return "error-page";
            }

            session.setAttribute("tokenResponse", tokenResponse);
            session.setAttribute("userResponse", userResponse);
            session.setAttribute("code", code);
            session.setAttribute("state", state);

            logger.info("Authenticated user: {} (ID: {})", userResponse.getLogin(), userResponse.getId());
        }

        // List<User> userList = campusUsersService.getAllCampusUsers(CAMPUS_ID, tokenResponse.getAccessToken());
        // session.setAttribute("userList", userList);

        // String kind = determineUserKind(userResponse);
        String kind = "admin";

        session.setAttribute("kind", kind);
        
        model.addAttribute("kind", kind);
        model.addAttribute("userResponse", userResponse);

        logger.info("Authentication successful for user: {} (Type: {})", userResponse.getLogin(), kind);

        return "certificat-page";
    }
    
    private String determineUserKind(User user) {
        if (user.getKind() != null) {
            return user.getKind();
        }

        if (isAdminUser(user)) {
            return "admin";
        }
        
        return "student";
    }
    
    private boolean isAdminUser(User user) {
        String[] adminLogins = {"admin", "root", "supervisor"};
        
        if (user.getLogin() != null) {
            for (String adminLogin : adminLogins) {
                if (user.getLogin().toLowerCase().contains(adminLogin)) {
                    return true;
                }
            }
        }
        
        return false;
    }
}