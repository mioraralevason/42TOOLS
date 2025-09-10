package com.ecole._2.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.exceptions.TemplateProcessingException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler({
        TemplateInputException.class,
        TemplateProcessingException.class,
        RuntimeException.class,
        Exception.class
    })
    public ModelAndView handleAllExceptions(Exception ex, HttpServletRequest request, HttpSession session) {
        logger.error("Global exception handler - URL: {} - Error: {}", 
                    request.getRequestURL(), ex.getMessage(), ex);
        
        // Si l'erreur est liée à une session invalide ou des données manquantes
        if (isSessionError(ex)) {
            logger.info("Session error detected, redirecting to login");
            // Nettoyer la session
            if (session != null) {
                session.invalidate();
            }
            return new ModelAndView("redirect:/login");
        }
        
        // Pour les autres erreurs, rediriger vers une page d'erreur
        ModelAndView modelAndView = new ModelAndView("error-page");
        modelAndView.addObject("error", "Une erreur inattendue s'est produite. Veuillez réessayer.");
        modelAndView.addObject("errorCode", "500");
        return modelAndView;
    }
    
    private boolean isSessionError(Exception ex) {
        String message = ex.getMessage();
        if (message == null) return false;
        
        return message.contains("session.userResponse") 
            || message.contains("Property or field 'image' cannot be found on null")
            || message.contains("userResponse")
            || (ex.getCause() != null && ex.getCause().getMessage() != null 
                && ex.getCause().getMessage().contains("cannot be found on null"));
    }
}