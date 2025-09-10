package com.ecole._2.controller;

import com.ecole._2.models.Event;
import com.ecole._2.models.User;
import com.ecole._2.services.ApiService;
import com.ecole._2.services.EventService;
import com.ecole._2.utils.EventCsvExporter;
import com.ecole._2.utils.SessionUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class EventController {
    
    private static final Logger logger = LoggerFactory.getLogger(EventController.class);
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private ApiService apiService;
    
    private static final String CAMPUS_ID = "65";
    
    /**
     * Display the events page with optional date filters (GET request)
     */
    @GetMapping("/events")
    public String showEventsPage(
            @RequestParam(value = "beginAt", required = false) String beginAt,
            @RequestParam(value = "endAt", required = false) String endAt,
            HttpSession session,
            Model model
    ) {
        // Vérifier la session avant de continuer
        if (!SessionUtils.isValidSession(session)) {
            logger.warn("Invalid session detected, redirecting to login");
            return "redirect:/login";
        }
        
        // Ajouter l'utilisateur au modèle pour éviter l'erreur Thymeleaf
        User userResponse = SessionUtils.getUserFromSession(session);
        model.addAttribute("userResponse", userResponse);
        model.addAttribute("kind", session.getAttribute("kind"));
        
        return fetchEvents(beginAt, endAt, session, model);
    }
    
    /**
     * Handle the form submission for filtering events (POST request)
     */
    @PostMapping("/events")
    public String filterEvents(
            @RequestParam(value = "beginAt", required = false) String beginAt,
            @RequestParam(value = "endAt", required = false) String endAt,
            HttpSession session,
            Model model
    ) {
        // Vérifier la session avant de continuer
        if (!SessionUtils.isValidSession(session)) {
            logger.warn("Invalid session detected, redirecting to login");
            return "redirect:/login";
        }
        
        // Ajouter l'utilisateur au modèle pour éviter l'erreur Thymeleaf
        User userResponse = SessionUtils.getUserFromSession(session);
        model.addAttribute("userResponse", userResponse);
        model.addAttribute("kind", session.getAttribute("kind"));
        
        return fetchEvents(beginAt, endAt, session, model);
    }
    
    @GetMapping("/events/export")
    public String exportEvents(HttpServletResponse response, HttpSession session, Model model) {
        try {
            // Vérifier la session avant de continuer
            if (!SessionUtils.isValidSession(session)) {
                logger.warn("Invalid session detected for export, redirecting to login");
                return "redirect:/login";
            }
            
            // Forcer UTF-8
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=events.csv");
            
            @SuppressWarnings("unchecked")
            List<Event> events = (List<Event>) session.getAttribute("events");
            
            if (events == null || events.isEmpty()) {
                logger.warn("No events found in session for export");
                model.addAttribute("error", "Aucun événement à exporter. Veuillez d'abord effectuer une recherche.");
                User userResponse = SessionUtils.getUserFromSession(session);
                model.addAttribute("userResponse", userResponse);
                model.addAttribute("kind", session.getAttribute("kind"));
                return fetchEvents(null, null, session, model);
            }
            
            try (OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
                // BOM pour Excel
                writer.write('\ufeff');
                EventCsvExporter.writeEventsToCsv(events, writer);
            }
            
            return null; // Pas de vue à rendre pour un download
            
        } catch (IOException e) {
            logger.error("Error during CSV export", e);
            model.addAttribute("error", "Erreur lors de l'export CSV: " + e.getMessage());
            User userResponse = SessionUtils.getUserFromSession(session);
            model.addAttribute("userResponse", userResponse);
            model.addAttribute("kind", session.getAttribute("kind"));
            return fetchEvents(null, null, session, model);
        }
    }
    
    /**
     * Common method to fetch events and add them to the model
     */
    private String fetchEvents(String beginAt, String endAt, HttpSession session, Model model) {
        try {
            // Get access token
            String token = apiService.getAccessToken();
            
            // Fetch events from EventService
            List<Event> events = eventService.getAllEvents(
                    CAMPUS_ID, // campusId fixed to 65
                    null, // cursusId optional
                    null, // userId optional
                    "begin_at", // sort by begin_at
                    beginAt,
                    endAt,
                    token,
                    100 // page size
            );
            
            session.setAttribute("events", events);
            model.addAttribute("events", events);
            model.addAttribute("beginAt", beginAt);
            model.addAttribute("endAt", endAt);
            model.addAttribute("searchPerformed", true);
            
            logger.info("Successfully fetched {} events", events.size());
            
        } catch (Exception e) {
            logger.error("Error fetching events", e);
            model.addAttribute("error", "Erreur lors de la récupération des événements: " + e.getMessage());
        }
        
        return "events-page"; // Thymeleaf template to display events
    }
}