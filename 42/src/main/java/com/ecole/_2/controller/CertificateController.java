package com.ecole._2.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.ecole._2.models.User;
import com.ecole._2.services.ApiService;
import com.ecole._2.services.CertificateService;
import com.ecole._2.services.UserCursusService;
import com.ecole._2.utils.CheckingUtils;

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

    @GetMapping("/certificate-generator")
    public ResponseEntity<?> getCertificate(
            @RequestParam("login") String login,
            @RequestParam(value = "sousigner_par", required = false, defaultValue = "Aucune") String sousignerPar,
            @RequestParam(value = "signer", required = false, defaultValue = "false") boolean signer,
            @RequestParam(value = "lang", required = false) String lang,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("userResponse");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        String kind = (String) session.getAttribute("kind");
        if (!"admin".equals(kind)) {
            login = user.getLogin();
        }

        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Login is required to generate the certificate.");
        }

        if (lang == null || (!"fr".equalsIgnoreCase(lang) && !"en".equalsIgnoreCase(lang))) {
            throw new IllegalArgumentException("Invalid or missing 'lang' parameter. Use 'fr' or 'en'.");
        }
        String langNormalized = lang.toLowerCase();

        String sousignerNormalized = "Aucune";
        if (sousignerPar != null) {
            String s = sousignerPar.trim().toUpperCase();
            if (s.equals("DG") || s.equals("DP") || s.equals("AP")) {
                sousignerNormalized = s;
            } else if (s.equalsIgnoreCase("aucune") || s.equalsIgnoreCase("none")) {
                sousignerNormalized = "Aucune";
            }
        }

        logger.info("Génération certificat: login={}, sousigner_par={}, signer={}, lang={}",
                login, sousignerNormalized, signer, langNormalized);

        Resource pdf = certificateService.generateCertificate(login, sousignerNormalized, signer, langNormalized);

        String filename = "school_certificate_" + login + "_" + langNormalized + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }

    @GetMapping("/certificate")
    public String auth(HttpSession session) {
        User userResponse = (User) session.getAttribute("userResponse");

        if (userResponse == null) {
            logger.warn("No user in session, redirecting to login");
            return "redirect:/login";
        }
        // String kind = CheckingUtils.determineUserKind(userResponse);
        String kind = "admin"; // Temporary override for testing
        session.setAttribute("kind", kind);
        logger.info("User {} (kind: {}) redirected to certificate page", userResponse.getLogin(), kind);

        return "redirect:" + FRONT_URL + "/certificate";
    }
}