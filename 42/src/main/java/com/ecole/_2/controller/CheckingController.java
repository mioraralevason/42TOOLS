package com.ecole._2.controller;

import com.ecole._2.models.LocationStat;
import com.ecole._2.models.TokenResponse;
import com.ecole._2.models.User;
import com.ecole._2.models.UserLocationStat;
import com.ecole._2.services.ApiService;
import com.ecole._2.services.CampusUsersService;
import com.ecole._2.services.UserLocationStatsFilterService;
import com.ecole._2.services.UserLocationStatsService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CheckingController {

    private static final Logger logger = LoggerFactory.getLogger(CheckingController.class);

    @Autowired
    private CampusUsersService campusUsersService;

    @Autowired
    private ApiService apiService;

    @Autowired
    private UserLocationStatsService userLocationStatsService;

    @Autowired
    private UserLocationStatsFilterService userLocationStatsFilterService;

    private static final String CAMPUS_ID = "65";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping("/check")
    public String checkPage(Model model, HttpSession session) {
        try {
            TokenResponse tokenResponse = (TokenResponse) session.getAttribute("tokenResponse");
            User userResponse = (User) session.getAttribute("userResponse");
            if (tokenResponse == null || userResponse == null) return "redirect:/login";

            model.addAttribute("userResponse", userResponse);
            model.addAttribute("kind", session.getAttribute("kind"));
            String today = LocalDate.now().format(DATE_FORMATTER);
            model.addAttribute("startDate", today);
            model.addAttribute("endDate", today);

        } catch (Exception e) {
            logger.error("Error loading checking page", e);
            model.addAttribute("error", "Erreur lors du chargement de la page");
            return "error-page";
        }
        return "checking-admin";
    }

    @PostMapping("/check")
    public String checkStudents(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "pool", required = false) String pool,
            @RequestParam(value = "year", required = false) String year,
            Model model,
            HttpSession session) {

        try {
            TokenResponse tokenResponse = (TokenResponse) session.getAttribute("tokenResponse");
            User userResponse = (User) session.getAttribute("userResponse");
            if (tokenResponse == null || userResponse == null) return "redirect:/login";

            // Validation des dates
            if (!isValidDateRange(startDate, endDate)) {
                model.addAttribute("error", "Dates invalides. La date de début doit être antérieure ou égale à la date de fin.");
                model.addAttribute("userResponse", userResponse);
                model.addAttribute("kind", session.getAttribute("kind"));
                model.addAttribute("startDate", startDate);
                model.addAttribute("endDate", endDate);
                model.addAttribute("pool", pool);
                model.addAttribute("year", year);
                return "checking-admin";
            }

            List<User> userList = (List<User>) session.getAttribute("userList");
            if (userList == null) {
                logger.warn("User list is null in session");
                userList = new ArrayList<>();
            }
            if (pool != null && !pool.isEmpty() && year != null && !year.isEmpty()) {
                userList = User.filterUsersByPool(userList, pool, year);
            }

            // Récupération sécurisée des stats utilisateurs avec leurs noms
            List<UserLocationStat> userLocationStats = new ArrayList<>();
            String token = apiService.getAccessToken();
            for (User u : userList) {
                try {
                    UserLocationStat stat = userLocationStatsService.getUserLocationStats(u.getId(), token,startDate,endDate);
                    // Récupérer les données de l'utilisateur pour obtenir le login
                    Map<String, Object> userData = apiService.getUser(u.getId(), token);
                    String userName = (String) userData.getOrDefault("login", u.getId()); 
                    stat.setUserName(userName); 
                    userLocationStats.add(stat);
                } catch (Exception e) {
                    logger.warn("Could not fetch stats or user data for user {}: {}", u.getId(), e.getMessage());
                    UserLocationStat stat = new UserLocationStat(u.getId(), null);
                    stat.setUserName(u.getId()); // Fallback sur userId si erreur
                    userLocationStats.add(stat);
                }
            }

            model.addAttribute("userResponse", userResponse);
            model.addAttribute("kind", session.getAttribute("kind"));
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("pool", pool);
            model.addAttribute("year", year);
            model.addAttribute("dayCount", userLocationStats.size());
            model.addAttribute("userLocationStats", userLocationStats);
            model.addAttribute("searchPerformed", true);

        } catch (Exception e) {
            logger.error("Error during student checking process", e);
            model.addAttribute("error", "Erreur lors de la vérification des présences: " + e.getMessage());
        }

        return "checking-admin";
    }

    private boolean isValidDateRange(String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
            LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
            return !end.isBefore(start);
        } catch (DateTimeParseException e) {
            logger.warn("Invalid date format: startDate={}, endDate={}", startDate, endDate);
            return false;
        }
    }

    @GetMapping("/checkUser")
    public String checkUserPage(Model model, HttpSession session) {
        try {
            TokenResponse tokenResponse = (TokenResponse) session.getAttribute("tokenResponse");
            User userResponse = (User) session.getAttribute("userResponse");
            if (tokenResponse == null || userResponse == null) return "redirect:/login";

            model.addAttribute("userResponse", userResponse);
            model.addAttribute("kind", session.getAttribute("kind"));
            String today = LocalDate.now().format(DATE_FORMATTER);
            model.addAttribute("startDate", today);
            model.addAttribute("endDate", today);
            model.addAttribute("login", "");

        } catch (Exception e) {
            logger.error("Error loading checking page for user", e);
            model.addAttribute("error", "Erreur lors du chargement de la page");
            return "error-page";
        }
        return "checking-user";
    }

    @PostMapping("/checkUser")
    public String checkSingleUser(
            @RequestParam("login") String login,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            Model model,
            HttpSession session) {

        try {
            TokenResponse tokenResponse = (TokenResponse) session.getAttribute("tokenResponse");
            User userResponse = (User) session.getAttribute("userResponse");
            if (tokenResponse == null || userResponse == null) return "redirect:/login";

            if (!isValidDateRange(startDate, endDate)) {
                model.addAttribute("error", "Dates invalides. La date de début doit être antérieure ou égale à la date de fin.");
                model.addAttribute("userResponse", userResponse);
                model.addAttribute("kind", session.getAttribute("kind"));
                model.addAttribute("startDate", startDate);
                model.addAttribute("endDate", endDate);
                model.addAttribute("login", "");
                return "checking-user";
            }

            UserLocationStat userStat;
            String token = apiService.getAccessToken();
            String userId = apiService.getIdUsers(login, token);
            try {
                userStat = userLocationStatsService.getUserLocationStats(userId, token,startDate,endDate);
                userStat.setUserName(login);
            } catch (Exception e) {
                logger.warn("Could not fetch stats or user data for user {}: {}", userId, e.getMessage());
                userStat = new UserLocationStat(userId, null);
                userStat.setUserName(userId); // Fallback sur userId si erreur
            }

            model.addAttribute("userResponse", userResponse);
            model.addAttribute("userLocationStats", List.of(userStat));
            model.addAttribute("dayCount", userStat.getNbDays(startDate, endDate));
            model.addAttribute("hourCount", userStat.getTotalHours(startDate, endDate));
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("searchPerformed", true);
            model.addAttribute("login", login);

        } catch (Exception e) {
            logger.error("Error checking single user", e);
            model.addAttribute("error", "Erreur lors de la vérification: " + e.getMessage());
        }

        return "checking-user";
    }
}