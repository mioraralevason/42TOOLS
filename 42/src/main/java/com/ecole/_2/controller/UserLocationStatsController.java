package com.ecole._2.controller;

import com.ecole._2.models.CursusUser;
import com.ecole._2.models.Freeze;
import com.ecole._2.models.User;
import com.ecole._2.models.UserLocationStat;
import com.ecole._2.services.ApiService;
import com.ecole._2.services.CampusUserService;
import com.ecole._2.services.UserCursusService;
import com.ecole._2.services.UserLocationStatsService;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserLocationStatsController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserLocationStatsController.class);
    
    @Autowired
    private UserLocationStatsService userLocationStatsService;

    @Autowired
    private UserCursusService userCursusService;

    @Autowired
    private ApiService apiService;

    @Autowired
    CampusUserService campusUserService;

    private static final String DEFAULT_USER_ID = "211018"; 

    @GetMapping("/freeze")
    public String getLocationsStats(@RequestParam(value = "login", required = false) String login, 
                                   HttpSession session, Model model) {
        return processFreeze(login, session, model);
    }

    @GetMapping("/freeze-begin")
    public String getLocationsStatsBegin(HttpSession session, Model model) {
        return processFreeze(null, session, model);
    }
    
    private String processFreeze(String login, HttpSession session, Model model) {
        String userId = null;
        String kind = (String) session.getAttribute("kind");
        
        logger.info("Processing freeze - Login: {}, Kind: {}", login, kind);
        
        try {
            
            userId = determineUserId(login, kind, session);
            
            if (userId == null) {
                logger.error("Unable to determine user ID");
                model.addAttribute("error", "Unable to determine user");
                return new CertificateController().auth(model, session);
            }
            
            logger.info("User ID determined: {}", userId);
            
            
            String tokenAdmin = apiService.getAccessToken();
            CursusUser userCursus = userCursusService.getUserCursus(userId, tokenAdmin).filterByGrade("Cadet");
            UserLocationStat userLocationStat = userLocationStatsService.getUserLocationStats(userId, tokenAdmin,null,null);
            
            
            Freeze freeze = new Freeze();
            freeze.setA(userLocationStat.getNbDays(userCursus.getBegin_at(), null));
            freeze.setB(userLocationStat.getNbOpenDays(userCursus.getBegin_at(), null));
            freeze.setC(userLocationStat.getTotalHours(userCursus.getBegin_at(), null));
            freeze.setD(userCursus.getMilestone());

            
            model.addAttribute("freeze", freeze.calculFreeze());
            model.addAttribute("login", getStringValue(apiService.getUser(userId, tokenAdmin), "login", ""));
            
            
            if ("admin".equals(kind)) {
                model.addAttribute("locationStats", userLocationStat);
                model.addAttribute("userCursus", userCursus);
                model.addAttribute("listLogin", session.getAttribute("listLogin"));
            }
            
        } catch (Exception e) {
            logger.error("Error during freeze processing for userId: {}", userId, e);
            model.addAttribute("error", "Error retrieving data: " + e.getMessage());
            return new CertificateController().auth(model, session);
        }
        
        return "freeze-page";
    }
    
    private String determineUserId(String login, String kind, HttpSession session) {
        logger.info("Determining ID - Login: {}, Kind: {}", login, kind);
        
        
        if (kind == null || !"admin".equals(kind)) {
            User sessionUser = (User) session.getAttribute("userResponse");
            if (sessionUser != null && sessionUser.getId() != null) {
                logger.info("Using session ID: {}", sessionUser.getId());
                return sessionUser.getId();
            } else {
                logger.info("Using default ID: {}", DEFAULT_USER_ID);
                return DEFAULT_USER_ID;
            }
        }
        
        
        if ("admin".equals(kind)) {
            
            if (login != null && !login.trim().isEmpty()) {
                try {
                    String foundUserId = apiService.getIdUsers(login.trim(), apiService.getAccessToken());
                    logger.info("ID found for login {}: {}", login, foundUserId);
                    return foundUserId;
                } catch (Exception e) {
                    logger.error("Error searching for ID for login: {}", login, e);
                    throw new RuntimeException("User not found: " + login);
                }
            } else {
                
                logger.info("Admin without specified login, using default admin ID");
                return "203988"; 
            }
        }
        
        return null;
    }
    
    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        if (map == null) {
            logger.warn("Map is null for key: {}", key);
            return defaultValue;
        }
        Object value = map.get(key);
        if (value != null) {
            logger.debug("Value found for {}: {}", key, value);
            return value.toString();
        } else {
            logger.warn("Value is null for key: {}", key);
            return defaultValue;
        }
    }
}