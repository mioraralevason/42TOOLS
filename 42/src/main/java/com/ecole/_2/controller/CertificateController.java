package com.ecole._2.controller;

import com.ecole._2.models.TokenResponse;
import com.ecole._2.models.User;
import com.ecole._2.services.CertificateService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class CertificateController {
    
    @Autowired
    private CertificateService certificateService;
    
    @GetMapping("/certificate-generator")
    public ResponseEntity<?> getCertificate(
            @RequestParam("login") String login,
            @RequestParam(value = "signer_par", defaultValue = "None") String signerPar,
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
            
            if (!signerPar.equalsIgnoreCase("None") &&
                !signerPar.equalsIgnoreCase("Director") &&
                !signerPar.equalsIgnoreCase("Assistant")) {
                signerPar = "None";
            }
            
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
            
            Resource pdf = certificateService.generateCertificate(login, signerPar);
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"school_certificate_" + login + ".pdf\"")
                .body(pdf);
                
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid parameter: " + e.getMessage());
            return ResponseEntity.status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
                
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error during generation: " + e.getMessage());
            return ResponseEntity.status(500)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
                
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error. Please try again later.");
            return ResponseEntity.status(500)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
        }
    }
    
    @GetMapping("/certificate")
    public String auth(Model model, HttpSession session) {
        User userResponse = (User) session.getAttribute("userResponse");
        
        if (userResponse == null) {
            return "redirect:/login";
        }
        model.addAttribute("userResponse", userResponse);

        // model.addAttribute("kind", userResponse.getKind());
        // session.setAttribute("kind", userResponse.getKind());

        model.addAttribute("kind", "admin");
        session.setAttribute("kind", "admin");
        
        return "certificat-page";
    }
}