package com.photoviewer.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * OpenAI GPT-4 Vision provider implementation.
 */
public class OpenAIProvider implements AIClient {
    private final String apiKey;
    private final ObjectMapper objectMapper;
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public OpenAIProvider(String apiKey) {
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String sendMessage(String prompt, BufferedImage image) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(API_URL);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Authorization", "Bearer " + apiKey);

            String requestBody;
            if (image != null) {
                String base64Image = encodeImageToBase64(image);
                requestBody = String.format(
                        "{\"model\":\"gpt-4o\",\"messages\":[{\"role\":\"user\",\"content\":[" +
                                "{\"type\":\"text\",\"text\":\"%s\"}," +
                                "{\"type\":\"image_url\",\"image_url\":{\"url\":\"data:image/png;base64,%s\"}}" +
                                "]}],\"max_tokens\":500}",
                        escapeJson(prompt), base64Image);
            } else {
                requestBody = String.format(
                        "{\"model\":\"gpt-4o\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}],\"max_tokens\":500}",
                        escapeJson(prompt));
            }

            request.setEntity(new StringEntity(requestBody));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                if (jsonResponse.has("error")) {
                    return "Error: " + jsonResponse.get("error").get("message").asText();
                }

                return jsonResponse.get("choices").get(0).get("message").get("content").asText();
            }
        }
    }

    @Override
    public String executeCommand(String command) throws Exception {
        return "Command execution not yet implemented";
    }

    private String encodeImageToBase64(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
