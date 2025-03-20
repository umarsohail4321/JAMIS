package com.app.jamis;
import android.util.Log;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FcmSender {
    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/careercampus-3e43a/messages:send";

    // Send notification method
    public static void sendNotification(String recipientToken, String employeeName, String jobCategory, String type, String status) throws Exception {

        try {
            // Get the access token from AccessTokenProvider
            String accessToken = AccessToken.getAccessToken();
            if (accessToken == null) {
                System.out.println("Failed to obtain access token");
                return;
            }

            // Build the appropriate JSON message payload based on the type
            String jsonMessage = buildJsonMessage(recipientToken, employeeName, jobCategory, type, status);

            Log.d("NOTI_TAG", "sendNotification: JSON: " + jsonMessage);

            // Prepare the connection to send the request
            URL url = new URL(FCM_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Write the JSON message payload
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonMessage.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Check response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Notification sent successfully.");
            } else {
                System.out.println("Failed to send notification. Response code: " + responseCode);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Build JSON message based on notification type
    private static String buildJsonMessage(String recipientToken, String employeeName, String jobCategory, String type, String status) {
        String title;
        String body;
        Map<String, String> data = new HashMap<>();

        if (type.equals("job_application")) {
            // New job application notification
            title = "New Job Application";
            body = employeeName + " has applied for the " + jobCategory + " position.";
            data.put("employeeName", employeeName);
            data.put("jobCategory", jobCategory);
        } else if (type.equals("application_update")) {
            // Job application status update (accept/reject)
            title = "Job Application Update";
            if (status.equals("accepted")) {
                body = "Congratulations " + employeeName + ", you have been accepted for the " + jobCategory + " position!";
            } else {
                body = "Sorry " + employeeName + ", you have been rejected for the " + jobCategory + " position.";
            }
            data.put("employeeName", employeeName);
            data.put("jobCategory", jobCategory);
            data.put("status", status);
        } else {
            throw new IllegalArgumentException("Invalid notification type");
        }

        // Build the JSON payload
        return "{"
                + "\"message\": {"
                + "\"token\": \"" + recipientToken + "\","
                + "\"notification\": {"
                + "\"title\": \"" + title + "\","
                + "\"body\": \"" + body + "\""
                + "},"
                + "\"data\": {"
                + "\"employeeName\": \"" + employeeName + "\","
                + "\"jobCategory\": \"" + jobCategory + "\","
                + "\"status\": \"" + status + "\""
                + "}"
                + "}"
                + "}";
    }
}




