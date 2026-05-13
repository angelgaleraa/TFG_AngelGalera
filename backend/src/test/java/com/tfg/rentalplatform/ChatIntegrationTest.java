package com.tfg.rentalplatform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void itemConversationWithoutReservationAcceptsMessages() throws Exception {
        String renterToken = login("luis@test.com", "1234");
        String ownerToken = login("ana@test.com", "1234");
        long ownerMessageNotificationsBefore = countMessageNotifications(ownerToken);

        String conversationResponse = mockMvc.perform(post("/api/chats/items/1")
                        .header("Authorization", "Bearer " + renterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value(1))
                .andExpect(jsonPath("$.itemTitle").value("Taladro Bosch"))
                .andExpect(jsonPath("$.reservationStatus").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long conversationId = objectMapper.readTree(conversationResponse).get("conversationId").asLong();

        mockMvc.perform(post("/api/chats/" + conversationId + "/messages")
                        .header("Authorization", "Bearer " + renterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Hola, me interesa\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(conversationId))
                .andExpect(jsonPath("$.content").value("Hola, me interesa"));

        mockMvc.perform(get("/api/chats/" + conversationId + "/messages")
                        .header("Authorization", "Bearer " + renterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hola, me interesa"));

        long ownerMessageNotificationsAfter = countMessageNotifications(ownerToken);
        org.assertj.core.api.Assertions.assertThat(ownerMessageNotificationsAfter)
                .isEqualTo(ownerMessageNotificationsBefore);
    }

    private long countMessageNotifications(String token) throws Exception {
        String response = mockMvc.perform(get("/api/notifications/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long count = 0;
        for (JsonNode notification : objectMapper.readTree(response)) {
            if ("MESSAGE_RECEIVED".equals(notification.get("type").asText())) {
                count++;
            }
        }
        return count;
    }

    private String login(String email, String password) throws Exception {
        String body = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }
}
