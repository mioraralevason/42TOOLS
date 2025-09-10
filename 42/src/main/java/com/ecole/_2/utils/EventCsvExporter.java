package com.ecole._2.utils;

import com.ecole._2.models.Event;

import java.io.IOException;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventCsvExporter {

    private static final String SEPARATOR = ";";

    public static void writeEventsToCsv(List<Event> events, Writer writer) throws IOException {
        // BOM UTF-8 pour Excel
        writer.write('\ufeff');
        // En-tête
        writer.write(String.join(SEPARATOR,
                "id", "name", "location", "kind", "nbr_subscribers", "begin_at", "end_at"));
        writer.write("\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Event event : events) {
            writer.write(escapeCsv(event.getId()) + SEPARATOR
                    + escapeCsv(event.getName()) + SEPARATOR
                    + escapeCsv(event.getLocation()) + SEPARATOR
                    + escapeCsv(event.getKind()) + SEPARATOR
                    + (event.getNbrSubscribers() != null ? event.getNbrSubscribers().toString() : "") + SEPARATOR
                    + (event.getBeginAt() != null ? formatOffsetDateTime(event.getBeginAt(), formatter) : "") + SEPARATOR
                    + (event.getEndAt() != null ? formatOffsetDateTime(event.getEndAt(), formatter) : "")
            );
            writer.write("\n");
        }
    }

    private static String formatOffsetDateTime(OffsetDateTime dateTime, DateTimeFormatter formatter) {
        return dateTime != null ? formatter.format(dateTime) : "";
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        
        // Nettoyer tous les caractères problématiques
        String cleaned = value
            .replace("\t", " ")     // Tabulations → espaces
            .replace("/", "-")      // Slashes → tirets
            .replace("@", "at")     // @ → "at"
            .replace("?", "");      // ? → supprimé
            
        String escaped = cleaned.replace("\"", "\"\"");
        if (escaped.contains(SEPARATOR) || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
