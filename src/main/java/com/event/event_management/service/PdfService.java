package com.event.event_management.service;

import com.event.event_management.entity.Event;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {
    
    public byte[] generateEventPdf(Event event) throws IOException {
        String html = generateEventHtml(event);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, outputStream);
        return outputStream.toByteArray();
    }
    
    private String generateEventHtml(Event event) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String dateTime = event.getDateTime() != null ? event.getDateTime().format(formatter) : "N/A";
        
        // Build resources section
        StringBuilder resourcesHtml = new StringBuilder();
        try {
            if (event.getResources() != null && !event.getResources().isEmpty()) {
                resourcesHtml.append("<tr><th>Assigned Resources</th><td><ul style='margin: 0; padding-left: 20px;'>");
                for (var resource : event.getResources()) {
                    resourcesHtml.append("<li>").append(escapeHtml(resource.getName()))
                               .append(" - Quantity: ").append(resource.getQuantity());
                    if (resource.getType() != null && !resource.getType().isEmpty()) {
                        resourcesHtml.append(" (Type: ").append(escapeHtml(resource.getType())).append(")");
                    }
                    if (resource.getNote() != null && !resource.getNote().isEmpty()) {
                        resourcesHtml.append(" - Note: ").append(escapeHtml(resource.getNote()));
                    }
                    resourcesHtml.append("</li>");
                }
                resourcesHtml.append("</ul></td></tr>");
            } else {
                resourcesHtml.append("<tr><th>Assigned Resources</th><td>No resources assigned</td></tr>");
            }
        } catch (Exception e) {
            resourcesHtml.append("<tr><th>Assigned Resources</th><td>Unable to load resources</td></tr>");
        }
        
        // Build updates section
        StringBuilder updatesHtml = new StringBuilder();
        try {
            if (event.getUpdates() != null && !event.getUpdates().isEmpty()) {
                updatesHtml.append("<tr><th>Updates & Notifications</th><td><ul style='margin: 0; padding-left: 20px;'>");
                DateTimeFormatter updateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                for (var update : event.getUpdates()) {
                    updatesHtml.append("<li>").append(escapeHtml(update.getDescription()));
                    if (update.getDate() != null) {
                        updatesHtml.append(" - ").append(update.getDate().format(updateFormatter));
                    }
                    updatesHtml.append("</li>");
                }
                updatesHtml.append("</ul></td></tr>");
            } else {
                updatesHtml.append("<tr><th>Updates & Notifications</th><td>No updates available</td></tr>");
            }
        } catch (Exception e) {
            updatesHtml.append("<tr><th>Updates & Notifications</th><td>Unable to load updates</td></tr>");
        }
        
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; margin: 40px; }" +
                "h1 { color: #c41e3a; border-bottom: 3px solid #c41e3a; padding-bottom: 10px; margin-bottom: 30px; }" +
                "h2 { color: #333; margin-top: 30px; margin-bottom: 15px; font-size: 18px; }" +
                "table { width: 100%; border-collapse: collapse; margin-top: 20px; }" +
                "th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; vertical-align: top; }" +
                "th { background-color: #f8f9fa; font-weight: bold; width: 200px; }" +
                "td { background-color: #ffffff; }" +
                ".status { padding: 5px 15px; border-radius: 20px; display: inline-block; font-weight: bold; }" +
                ".status-planned { background-color: #17a2b8; color: white; }" +
                ".status-new { background-color: #28a745; color: white; }" +
                ".status-in-progress { background-color: #f8b500; color: #212529; }" +
                ".status-completed { background-color: #2c5530; color: white; }" +
                ".status-cancelled { background-color: #dc3545; color: white; }" +
                "ul { margin: 5px 0; }" +
                "li { margin: 5px 0; }" +
                ".footer { margin-top: 40px; text-align: center; color: #666; font-size: 12px; border-top: 1px solid #ddd; padding-top: 20px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h1>Event Details Report</h1>" +
                "<h2>Basic Information</h2>" +
                "<table>" +
                "<tr><th>Event Name</th><td>" + escapeHtml(event.getName()) + "</td></tr>" +
                "<tr><th>Venue</th><td>" + escapeHtml(event.getVenue()) + "</td></tr>" +
                "<tr><th>Date & Time</th><td>" + dateTime + "</td></tr>" +
                "<tr><th>Guest Count</th><td>" + event.getGuestCount() + "</td></tr>" +
                "<tr><th>Status</th><td><span class='status status-" + 
                (event.getStatus() != null ? event.getStatus().toLowerCase().replace(" ", "-") : "planned") + 
                "'>" + (event.getStatus() != null ? event.getStatus() : "Planned") + "</span></td></tr>" +
                "<tr><th>Description</th><td>" + escapeHtml(event.getDescription() != null ? event.getDescription() : "N/A") + "</td></tr>" +
                "</table>" +
                "<h2>Resources & Updates</h2>" +
                "<table>" +
                resourcesHtml.toString() +
                updatesHtml.toString() +
                "</table>" +
                "<div class='footer'>" +
                "<p><strong>Generated by Golden Dish Caterings Event Management System</strong></p>" +
                "<p>Report generated on: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
}
