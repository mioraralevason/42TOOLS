package com.ecole._2.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecole._2.models.User;
import com.ecole._2.services.ApiService;
import com.ecole._2.services.CertificateService;
import com.ecole._2.services.UserCursusService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CertificateController {

    private static final Logger logger = LoggerFactory.getLogger(CertificateController.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private UserCursusService userCursusService;

    @Autowired
    private ApiService apiService;

    private static final String FRONT_URL = "http://localhost:5173";

    /**
     * Endpoint pour générer le certificat.
     * NOTE: 'lang' est optional pour éviter l'exception Spring si quelqu'un appelle l'URL
     *       directement. Nous faisons ensuite une validation explicite et renvoyons une
     *       erreur 400 si la langue est absente ou invalide.
     */
    @GetMapping("/certificate-generator")
    public ResponseEntity<?> getCertificate(
            @RequestParam("login") String login,
            @RequestParam(value = "signer_par", required = false, defaultValue = "Aucune") String signerPar,
            @RequestParam(value = "lang", required = false) String lang,
            HttpSession session,
            Model model
    ) {
        try {
            User user = (User) session.getAttribute("userResponse");
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Session expired. Please log in again.");
                return ResponseEntity.status(401)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
            }
            
            // Si l'utilisateur n'est pas admin, on force le login de session (sécurité)
            if (!"admin".equals(session.getAttribute("kind"))) {
                login = user.getLogin();
            }
            
            if (login == null || login.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Login is required to generate the certificate.");
                return ResponseEntity.status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
            }
            
            
            System.out.println("langue choisi: "+ lang);
            // Validation/langue : on exige explicitement "fr" ou "en"
            if (lang == null || (!"fr".equalsIgnoreCase(lang) && !"en".equalsIgnoreCase(lang))) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid or missing 'lang' parameter. Use 'fr' or 'en'.");
                return ResponseEntity.status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
            }
            String langNormalized = lang.toLowerCase();
            
            // Normalisation signer_par : accepte valeurs FR ou EN envoyées par le formulaire
            String signerNormalized = "Aucune"; // valeur canonique par défaut
            if (signerPar != null) {
                String s = signerPar.trim();
                if (s.equalsIgnoreCase("directeur") || s.equalsIgnoreCase("director")) {
                    signerNormalized = "Directeur";
                } else if (s.equalsIgnoreCase("assistant") || s.equalsIgnoreCase("assistant")) {
                    signerNormalized = "Assistant";
                } else if (s.equalsIgnoreCase("aucune") || s.equalsIgnoreCase("none")) {
                    signerNormalized = "Aucune";
                } else {
                    // si valeur inconnue, on ne plante pas : on remet Aucune
                    signerNormalized = "Aucune";
                }
            }
            
            logger.info("Génération certificat: login={}, signer_par={}, lang={}", login, signerNormalized, langNormalized);
            
            Resource pdf = certificateService.generateCertificate(login, signerNormalized, langNormalized);
            
            String filename = "school_certificate_" + login + "_" + langNormalized + ".pdf";
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(pdf);

        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid parameter: " + e.getMessage());
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);

        } catch (RuntimeException e) {
            logger.error("Runtime error during certificate generation", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error during generation: " + e.getMessage());
            return ResponseEntity.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);

        } catch (Exception e) {
            logger.error("Internal error during certificate generation", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error. Please try again later.");
            return ResponseEntity.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }

    @GetMapping("/certificate")
    public String auth(HttpSession session) {
        User userResponse = (User) session.getAttribute("userResponse");

        if (userResponse == null) {
            logger.warn("No user in session, redirecting to login");
            return "redirect:/login";
        }

        String kind = determineUserKind(userResponse);
        session.setAttribute("kind", kind);
        logger.info("User {} (kind: {}) redirected to certificate page", userResponse.getLogin(), kind);

        // Redirect to React frontend route
        return "redirect:" + FRONT_URL + "/certificate";
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
