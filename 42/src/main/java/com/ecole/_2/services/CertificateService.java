package com.ecole._2.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class CertificateService {

    @Autowired
    private ApiService apiService;

    @Value("${certificate.logo:Inconnu.png}")
    private String logoPath;

    @Value("${certificate.tampon:Inconnu.png}")
    private String tamponPath;

    @Value("${certificate.signature.directeur:Inconnu.png}")
    private String signatureDirecteurPath;

    @Value("${certificate.signature.assistant:Inconnu.png}")
    private String signatureAssistantPath;

    @Value("${certificate.responsable:Inconnu}")
    private String responsable;

    @Value("${certificate.poste:Inconnu}")
    private String poste;

    @Value("${certificate.etablissement:42}")
    private String etablissement;

    @Value("${certificate.etablissement.adresse:Inconnu}")
    private String etablissementAdresse;

    private static final Map<String, String> COUNTRY_TO_NATIONALITY = new HashMap<>();
    static {
        COUNTRY_TO_NATIONALITY.put("France", "Française");
        COUNTRY_TO_NATIONALITY.put("Madagascar", "Malgache");
        COUNTRY_TO_NATIONALITY.put("Spain", "Espagnole");
        COUNTRY_TO_NATIONALITY.put("Italy", "Italienne");
        COUNTRY_TO_NATIONALITY.put("Germany", "Allemande");
    }

    public Resource generateCertificate(String login, String signerPar) {
        try {
            if (login == null || login.trim().isEmpty()) {
                throw new IllegalArgumentException("Le login ne peut pas être vide");
            }
            
            if (!login.matches("^[a-zA-Z0-9_]+$")) {
                throw new IllegalArgumentException("Le login ne doit contenir que des lettres, chiffres ou underscores");
            }
            
            if (!"Directeur".equals(signerPar) && !"Assistant".equals(signerPar) && !"Aucune".equals(signerPar)) {
                signerPar = "Aucune";
            }

            String token = apiService.getAccessToken();
            String userId = apiService.getIdUsers(login, token);
            Map<String, Object> userData = apiService.getUser(userId, token);
            Map<String, Object> candidatureData = apiService.getUserCandidature(userId, token);

            ByteArrayOutputStream outputStream = createPdfDocument(userData, candidatureData, signerPar);
            
            return new ByteArrayResource(outputStream.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du certificat pour " + login + ": " + e.getMessage(), e);
        }
    }

    private ByteArrayOutputStream createPdfDocument(Map<String, Object> userData, Map<String, Object> candidatureData, String signerPar) throws DocumentException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 56.7f, 56.7f, 113.4f, 113.4f);
        
        try {
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    try {
                        drawImages(writer.getDirectContent(), signerPar);
                    } catch (Exception e) {
                        System.err.println("Erreur lors du dessin des images: " + e.getMessage());
                    }
                }
            });

            document.open();
            
            addContent(document, userData, candidatureData, signerPar);
            
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        
        return outputStream;
    }

    private void addContent(Document document, Map<String, Object> userData, Map<String, Object> candidatureData, String signerPar) throws DocumentException {
        try {
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            Font footerFont2 = FontFactory.getFont(FontFactory.HELVETICA, 10, new BaseColor(39, 221, 245));

            Paragraph title = new Paragraph("Certificat de scolarité", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            String nom = getStringValue(userData, "last_name", "Inconnu");
            String prenom = getStringValue(userData, "first_name", "Inconnu");
            String dateNaissance = formatDateNaissance(getStringValue(candidatureData, "birth_date", "2000-01-01"));
            String lieuNaissance = getStringValue(candidatureData, "birth_city", "Inconnu");
            String adresse = getStringValue(candidatureData, "postal_street", "Inconnu");
            String zipCode = getStringValue(candidatureData, "postal_zip_code", "00000");
            String ville = getStringValue(candidatureData, "postal_city", "Inconnu");
            String nationalite = COUNTRY_TO_NATIONALITY.getOrDefault(getStringValue(candidatureData, "country", "Inconnu"), "Inconnue");
            
            String gender = getStringValue(candidatureData, "gender", "");
            String monsieurMadame = "male".equals(gender) ? "Monsieur" : "female".equals(gender) ? "Madame" : "Monsieur";
            String interesse = "male".equals(gender) ? "l'intéressé" : "female".equals(gender) ? "l'intéressée" : "l'intéressé";
            String inscrit = "male".equals(gender) ? "inscrit" : "female".equals(gender) ? "inscrite" : "inscrit";

            String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            String currentYear = String.valueOf(LocalDate.now().getYear());

            Paragraph content = new Paragraph();
            content.setAlignment(Element.ALIGN_LEFT);
            content.setSpacingAfter(12f);
            
            content.add(new Chunk("Je soussigné, Monsieur ", bodyFont));
            content.add(new Chunk(responsable + ", " + poste, boldFont));
            content.add(new Chunk(" de l'établissement " + etablissement + ", domicilié au " + etablissementAdresse + ", atteste que l'élève :", bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(Chunk.NEWLINE);
            
            content.add(new Chunk(monsieurMadame, boldFont));
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Nom", boldFont));
            content.add(new Chunk(" : " + nom, bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Prénom", boldFont));
            content.add(new Chunk(" : " + prenom, bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Date de naissance", boldFont));
            content.add(new Chunk(" : " + dateNaissance, bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Lieu de naissance", boldFont));
            content.add(new Chunk(" : " + lieuNaissance, bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Nationalité", boldFont));
            content.add(new Chunk(" : " + nationalite, bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Adresse", boldFont));
            content.add(new Chunk(" : " + adresse + ", " + zipCode + " " + ville, bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(Chunk.NEWLINE);
            
            content.add(new Chunk("Est régulièrement " + inscrit + " pour l'année " + currentYear + " à l'école " + etablissement + ", école gratuite sans frais d'écolage, et n'y a pas encore fini ses études.", bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Cette attestation est délivrée le " + currentDate + " à la demande de " + interesse + " pour servir et faire valoir ce que de droit.", bodyFont));
            
            document.add(content);

            document.add(new Paragraph(" ", bodyFont));
            document.add(new Paragraph(" ", bodyFont));

            Paragraph footer = new Paragraph();
            footer.add(new Chunk("BY ", footerFont));
            Chunk etablissementChunk = new Chunk(etablissement, footerFont2);
            etablissementChunk.setUnderline(0.5f, -2f);
            footer.add(etablissementChunk);
            footer.add(Chunk.NEWLINE);
            footer.add(new Chunk(etablissementAdresse, footerFont));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(40f);
            
            document.add(footer);
            
        } catch (Exception e) {
            throw new DocumentException("Erreur lors de la création du contenu: " + e.getMessage(), e);
        }
    }

    private void drawImages(PdfContentByte canvas, String signerPar) {
        try {
            float pageWidth = PageSize.A4.getWidth();
            float pageHeight = PageSize.A4.getHeight();
            
            try {
                if (logoPath != null && !logoPath.equals("Inconnu.png") && Files.exists(Paths.get(logoPath))) {
                    Image logo = Image.getInstance(logoPath);
                    logo.scaleToFit(pageWidth - 113.4f, 113.4f);
                    
                    float logoWidth = logo.getScaledWidth();
                    float logoX = (pageWidth - logoWidth) / 2;  
                    float logoY = pageHeight - logo.getScaledHeight();

                    logo.setAbsolutePosition(logoX, logoY);
                    canvas.addImage(logo);
                }
            } catch (Exception e) {
                System.err.println("Erreur logo: " + e.getMessage());
            }

            try {
                if (tamponPath != null && !tamponPath.equals("Inconnu.png") && Files.exists(Paths.get(tamponPath))) {
                    Image tampon = Image.getInstance(tamponPath);
                    tampon.scaleToFit(170.1f, 105.8f);

                    float tamponWidth = tampon.getScaledWidth();
                    float tamponX = pageWidth - tamponWidth - 56.7f;
                    float tamponY = 170.1f;

                    tampon.setAbsolutePosition(tamponX, tamponY);
                    canvas.addImage(tampon);
                }
            } catch (Exception e) {
                System.err.println("Erreur tampon: " + e.getMessage());
            }
            
            try {
                String signaturePath = null;
                if ("Directeur".equals(signerPar)) {
                    signaturePath = signatureDirecteurPath;
                } else if ("Assistant".equals(signerPar)) {
                    signaturePath = signatureAssistantPath;
                }
                
                if (signaturePath != null && !signaturePath.equals("Inconnu.png") && Files.exists(Paths.get(signaturePath))) {
                    Image signature = Image.getInstance(signaturePath);
                    signature.scaleToFit(170.1f, 85.05f);
                    signature.setAbsolutePosition(56.7f, 170.1f);
                    canvas.addImage(signature);
                }
            } catch (Exception e) {
                System.err.println("Erreur signature: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Erreur générale lors du dessin des images: " + e.getMessage());
        }
    }

    private String formatDateNaissance(String birthDate) {
        try {
            if (birthDate == null || birthDate.trim().isEmpty()) {
                System.out.println("Aucune date de naissance trouvée, utilisation de la date par défaut '01/01/2000'");
                return "01/01/2000";
            }
            
            LocalDate date = LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            
        } catch (Exception e) {
            System.err.println("Format de date de naissance invalide: " + e.getMessage());
            return "01/01/2000";
        }
    }

    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        if (map == null) return defaultValue;
        Object value = map.get(key);
        return (value != null) ? value.toString() : defaultValue;
    }
}