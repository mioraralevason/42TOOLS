package com.ecole._2.controller;

import com.ecole._2.models.CursusUser;
import com.ecole._2.models.Freeze;
import com.ecole._2.models.TokenResponse;
import com.ecole._2.models.User;
import com.ecole._2.models.UserLocationStat;
import com.ecole._2.services.ApiService;
import com.ecole._2.services.UserCursusService;
import com.ecole._2.services.UserLocationStatsService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalendarController {

    private static final Logger logger = LoggerFactory.getLogger(CalendarController.class);
    private static final String CAMPUS_ID = "65";

    @Autowired
    private UserLocationStatsService userLocationStatsService;

    @Autowired
    private UserCursusService userCursusService;

    @Autowired
    private ApiService apiService;

    @GetMapping("/calendar")
    public String calendarPage(
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @RequestParam(value = "login", required = false) String login,
            HttpSession session,
            Model model) {

        try {
            TokenResponse tokenResponse = (TokenResponse) session.getAttribute("tokenResponse");
            User userResponse = (User) session.getAttribute("userResponse");
            if (tokenResponse == null || userResponse == null) return "redirect:/login";

            String accessToken = tokenResponse.getAccessToken();
            String userId = userResponse.getId();
            boolean isAdmin = "admin".equalsIgnoreCase((String) session.getAttribute("kind"));

            // si admin => peut consulter un autre user via login, sinon par défaut lui
            String targetUserId = userId;
            if (isAdmin && login != null && !login.isEmpty()) {
                targetUserId = apiService.getIdUsers(login, accessToken);
            }

            // Récupération cursus
            CursusUser cursus = userCursusService.getUserCursus(targetUserId, accessToken)
                    .filterByGrade("Cadet");

            LocalDate monthStart = LocalDate.of(year, month, 1);
            LocalDate startDate = cursus.getBeginZonedDateTime().toLocalDate().isAfter(monthStart)
                    ? cursus.getBeginZonedDateTime().toLocalDate()
                    : monthStart;
            LocalDate endDate = (cursus.getBlackholed_at() != null && !cursus.getBlackholed_at().isEmpty())
                    ? cursus.getBlackholedZonedDateTime().toLocalDate()
                    : monthStart.plusMonths(1).withDayOfMonth(monthStart.lengthOfMonth());

            // Stats de présence
            UserLocationStat locationStats = userLocationStatsService.getUserLocationStats(
                    targetUserId, accessToken, startDate.toString(), endDate.toString()
            );

            List<String> presenceDates = new ArrayList<>();
            if (locationStats != null && locationStats.getStats() != null) {
                locationStats.getStats().forEach(stat -> presenceDates.add(stat.getDate().toString()));
            }

            // Calcul freeze
            Freeze freeze = new Freeze();
            freeze.setA(locationStats.getNbDays(cursus.getBegin_at(), cursus.getBlackholed_at()));
            freeze.setB(locationStats.getNbOpenDays(cursus.getBegin_at(), cursus.getBlackholed_at()));
            freeze.setC(locationStats.getTotalHours(cursus.getBegin_at(), cursus.getBlackholed_at()));
            freeze.setD(cursus.getMilestone());
            double freezeValue = freeze.calculFreeze();

            // Milestones
            List<Map<String, String>> milestones = new ArrayList<>();
            if (cursus.getBlackholed_at() != null && !cursus.getBlackholed_at().isEmpty()) {
                milestones.add(Map.of(
                        "date", cursus.getBlackholed_at().substring(0, 10),
                        "label", "Deadline (Blackhole)"
                ));
            }

            // Injecte les données dans le modèle
            model.addAttribute("userResponse", userResponse);
            model.addAttribute("kind", session.getAttribute("kind"));
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("presence", presenceDates);
            model.addAttribute("freeze", freezeValue);
            model.addAttribute("milestones", milestones);
            model.addAttribute("targetUserId", targetUserId);
            model.addAttribute("login", login);
            model.addAttribute("isAdmin", isAdmin);

            return isAdmin ? "calendar-admin" : "calendar-user";

        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du calendrier", e);
            model.addAttribute("error", "Erreur lors de la récupération du calendrier: " + e.getMessage());
            return "error-page";
        }
    }
}
