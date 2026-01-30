package com.email.email_writter_sb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailGeneratorService {

    private final WebClient webClient;

    @Value("${groq.api.url}")
    private String groqUrl;

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.model}")
    private String model;

    public String generateEmailReply(EmailRequest emailRequest) {

        try {
            String prompt = buildPrompt(emailRequest);

            // ✅ CORRECT JSON STRUCTURE FOR GROQ
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", prompt
                            )
                    ),
                    "temperature", 0.7
            );

            String response = webClient.post()
                    .uri(groqUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + groqApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Groq raw response: {}", response);

            return extractText(response);

        } catch (Exception e) {
            log.error("Groq API error", e);
            return "Error generating email reply. Please try again later.";
        }
    }

    // ✅ SAFE JSON PARSING (NO 500)
    private String extractText(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            return root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .trim();

        } catch (Exception e) {
            log.error("Response parsing error", e);
            return "Error parsing AI response.";
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        return """
                You are a professional email assistant.

                Rules:
                - Do NOT include a subject line
                - Be polite and professional
                - Use a %s tone

                Original email:
                %s
                """.formatted(
                emailRequest.getTone() == null ? "professional" : emailRequest.getTone(),
                emailRequest.getEmailContent()
        );
    }
}
