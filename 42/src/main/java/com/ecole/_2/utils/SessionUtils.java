package com.ecole._2.utils;

import com.ecole._2.models.TokenResponse;
import com.ecole._2.models.User;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionUtils.class);
    
    /**
     * Vérifie si la session contient les données utilisateur nécessaires
     */
    public static boolean isValidSession(HttpSession session) {
        if (session == null) {
            logger.warn("Session is null");
            return false;
        }
        
        TokenResponse tokenResponse = (TokenResponse) session.getAttribute("tokenResponse");
        User userResponse = (User) session.getAttribute("userResponse");
        
        boolean isValid = tokenResponse != null && userResponse != null;
        
        if (!isValid) {
            logger.warn("Invalid session - tokenResponse: {}, userResponse: {}", 
                       tokenResponse != null, userResponse != null);
        }
        
        return isValid;
    }
    
    /**
     * Récupère l'utilisateur de la session de manière sécurisée
     */
    public static User getUserFromSession(HttpSession session) {
        if (!isValidSession(session)) {
            return null;
        }
        return (User) session.getAttribute("userResponse");
    }
    
    /**
     * Récupère le token de la session de manière sécurisée
     */
    public static TokenResponse getTokenFromSession(HttpSession session) {
        if (!isValidSession(session)) {
            return null;
        }
        return (TokenResponse) session.getAttribute("tokenResponse");
    }
    
    /**
     * Nettoie complètement la session
     */
    public static void clearSession(HttpSession session) {
        if (session != null) {
            try {
                session.invalidate();
                logger.info("Session cleared successfully");
            } catch (Exception e) {
                logger.error("Error clearing session", e);
            }
        }
    }
}