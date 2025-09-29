package com.ecole._2.controller;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ecole._2.models.CursusUser;
import com.ecole._2.models.LocationStat;
import com.ecole._2.models.TokenResponse;
import com.ecole._2.models.User;
import com.ecole._2.models.UserLocationStat;
import com.ecole._2.services.ApiService;
import com.ecole._2.services.CampusUsersService;
import com.ecole._2.services.UserLocationStatsService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CheckingController {

    private static final Logger logger = LoggerFactory.getLogger(CheckingController.class);

    @Autowired
    private CampusUsersService campusUsersService;

    @Autowired
    private ApiService apiService;

    @Autowired
    private UserLocationStatsService userLocationStatsService;

    private static final String CAMPUS_ID = "65";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping("/check")
    public String checkPage(Model model, HttpSession session) {
        try {
            TokenResponse tokenResponse = (TokenResponse) session.getAttribute("tokenResponse");
            User userResponse = (User) session.getAttribute("userResponse");
            if (tokenResponse == null || userResponse == null) {
                logger.warn("No token or user in session, redirecting to login");
                return "redirect:/login";
            }

            model.addAttribute("userResponse", userResponse);
            model.addAttribute("kind", session.getAttribute("kind"));
            String today = LocalDate.now().format(DATE_FORMATTER);
            model.addAttribute("startDate", today);
            model.addAttribute("endDate", today);

        } catch (Exception e) {
            logger.error("Error loading checking page", e);
            model.addAttribute("error", "Erreur lors du chargement de la page: " + e.getMessage());
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
            if (tokenResponse == null || userResponse == null) {
                logger.warn("No token or user in session, redirecting to login");
                return "redirect:/login";
            }

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

            List<UserLocationStat> userLocationStats = new ArrayList<>();
            String token = apiService.getAccessToken();
            for (User u : userList) {
                try {
                    UserLocationStat stat = userLocationStatsService.getUserLocationStats(u.getId(), token, startDate, endDate);
                    Map<String, Object> userData = apiService.getUser(u.getId(), token);
                    String userName = (String) userData.getOrDefault("login", u.getId());
                    stat.setUserName(userName);
                    userLocationStats.add(stat);
                } catch (Exception e) {
                    logger.warn("Could not fetch stats or user data for user {}: {}", u.getId(), e.getMessage());
                    UserLocationStat stat = new UserLocationStat(u.getId(), null);
                    stat.setUserName(u.getId());
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

    @GetMapping("/checkUser")
    public String checkUserPage(Model model, HttpSession session) {
        try {
            TokenResponse tokenResponse = (TokenResponse) session.getAttribute("tokenResponse");
            User userResponse = (User) session.getAttribute("userResponse");
            if (tokenResponse == null || userResponse == null) {
                logger.warn("No token or user in session, redirecting to login");
                return "redirect:/login";
            }

            model.addAttribute("userResponse", userResponse);
            model.addAttribute("kind", session.getAttribute("kind"));
            String today = LocalDate.now().format(DATE_FORMATTER);
            model.addAttribute("startDate", today);
            model.addAttribute("endDate", today);
            model.addAttribute("login", "");

        } catch (Exception e) {
            logger.error("Error loading checking page for user", e);
            model.addAttribute("error", "Erreur lors du chargement de la page: " + e.getMessage());
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
            if (tokenResponse == null || userResponse == null) {
                logger.warn("No token or user in session, redirecting to login");
                return "redirect:/login";
            }

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
                userStat = userLocationStatsService.getUserLocationStats(userId, token, startDate, endDate);
                userStat.setUserName(login);
                List<LocationStat> locationStats = userStat.filterStatsBetween(startDate, endDate);
                userStat.setStats(locationStats);
            } catch (Exception e) {
                logger.warn("Could not fetch stats or user data for user {}: {}", userId, e.getMessage());
                userStat = new UserLocationStat(userId, null);
                userStat.setUserName(userId);
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

    @GetMapping("/calendar")
    @ResponseBody
    public Map<String, Object> getCalendar(
            @RequestParam("year") int year,
            @RequestParam(value = "month", required = false, defaultValue = "0") int month,
            @RequestParam(value = "login", required = false) String login,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            TokenResponse tokenResponse = (TokenResponse) session.getAttribute("tokenResponse");
            if (tokenResponse == null) {
                logger.warn("No token in session for /calendar request");
                response.put("error", "Authentication required");
                return response;
            }

            String token = apiService.getAccessToken();
            logger.info("Fetching calendar for year: {}, month: {}, login: {}", year, month, login);

            String userId = login != null && !login.isEmpty() ? apiService.getIdUsers(login, token) : null;
            if (userId == null && login != null && !login.isEmpty()) {
                logger.warn("User not found for login: {}", login);
                response.put("error", "User not found for login: " + login);
                return response;
            }

            CursusUser cursusUser = null;
            List<Map<String, Object>> milestoneDates = new ArrayList<>();
            String formattedBlackholedAt = null;
            if (userId != null) {
                cursusUser = apiService.getCursusUser(userId, token);
                if (cursusUser == null) {
                    logger.warn("No CursusUser found for userId: {}", userId);
                    response.put("milestones", new ArrayList<>());
                    response.put("blackholed_at", null);
                    response.put("milestoneDates", milestoneDates);
                } else {
                    // Log des milestones pour débogage
                    logger.info("Raw milestones from CursusUser: {}", cursusUser.getMilestones());
                    // Ajouter les milestones
                    List<Map<String, Object>> milestones = cursusUser.getMilestones().stream()
                            .map(m -> {
                                Map<String, Object> milestoneData = new HashMap<>();
                                milestoneData.put("level", m.getLevel());
                                String formattedDate = m.getDate() != null ? formatMilestoneDate(m.getDate()) : null;
                                milestoneData.put("date", formattedDate);
                                logger.info("Processed milestone: level={}, date={}", m.getLevel(), formattedDate);
                                return milestoneData;
                            })
                            .collect(Collectors.toList());
                    response.put("milestones", milestones);

                    // Format blackholed_at to yyyy-MM-dd
                    formattedBlackholedAt = cursusUser.getBlackholed_at() != null
                            ? formatMilestoneDate(cursusUser.getBlackholed_at())
                            : null;
                    response.put("blackholed_at", formattedBlackholedAt);

                    // Extraire les dates de début des milestones
                    milestoneDates = cursusUser.getMilestones().stream()
                            .filter(m -> m.getDate() != null)
                            .map(m -> {
                                Map<String, Object> milestoneInfo = new HashMap<>();
                                milestoneInfo.put("level", m.getLevel());
                                String formattedDate = formatMilestoneDate(m.getDate());
                                milestoneInfo.put("date", formattedDate);
                                logger.info("Processed milestoneDate: level={}, date={}", m.getLevel(), formattedDate);
                                return milestoneInfo;
                            })
                            .collect(Collectors.toList());
                    response.put("milestoneDates", milestoneDates);
                }
            } else {
                response.put("milestones", new ArrayList<>());
                response.put("blackholed_at", null);
                response.put("milestoneDates", milestoneDates);
            }

            LocalDate startDate;
            LocalDate endDate;

            if (month > 0) {
                startDate = LocalDate.of(year, month, 1);
                endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            } else {
                startDate = LocalDate.of(year, 1, 1);
                endDate = LocalDate.of(year, 12, 31);
            }

            List<LocationStat> stats = new ArrayList<>();
            if (userId != null) {
                UserLocationStat userStat = userLocationStatsService.getUserLocationStats(
                        userId, token, startDate.format(DATE_FORMATTER), endDate.format(DATE_FORMATTER));
                stats = userStat.filterStatsBetween(startDate.format(DATE_FORMATTER), endDate.format(DATE_FORMATTER));
            }

            List<String> presenceDays = stats.stream()
                    .filter(stat -> stat.getDuration() != null && !stat.getDuration().isZero())
                    .map(stat -> stat.getDate().format(DATE_FORMATTER))
                    .collect(Collectors.toList());

            response.put("presence", presenceDays);

            logger.info("Returning {} presence days and {} milestone dates for user {}", presenceDays.size(),
                    milestoneDates.size(), login);
            return response;

        } catch (Exception e) {
            logger.error("Error fetching calendar data for user {}: {}", login, e.getMessage(), e);
            response.put("error", "Erreur lors de la récupération des données du calendrier: " + e.getMessage());
            return response;
        }
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

    private String formatMilestoneDate(String dateStr) {
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(dateStr);
            return zdt.toLocalDate().format(DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            logger.warn("Invalid date format for milestone: {}", dateStr);
            return null;
        }
    }
}