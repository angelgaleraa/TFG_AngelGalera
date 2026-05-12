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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationPaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void reservationOverlapIsRejectedAndPaymentGeneratedOnAccept() throws Exception {
        String renterToken = login("luis@test.com", "1234");
        String ownerToken = login("ana@test.com", "1234");

        String createReservationBody = """
                {
                  "itemId": 1,
                  "startDate": "2030-01-10",
                  "endDate": "2030-01-12"
                }
                """;

        String reservationResponse = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + renterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createReservationBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long reservationId = objectMapper.readTree(reservationResponse).get("id").asLong();

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + renterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createReservationBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RESERVATION_OVERLAP"));

        mockMvc.perform(put("/api/reservations/" + reservationId + "/accept")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        mockMvc.perform(get("/api/payments/me")
                        .header("Authorization", "Bearer " + renterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CAPTURED"));
    }

    @Test
    void reservationEndingWhenAnotherStartsIsAllowed() throws Exception {
        String renterToken = login("luis@test.com", "1234");
        String otherRenterToken = login("carlos@test.com", "1234");

        String firstReservation = """
                {
                  "itemId": 1,
                  "startDate": "2031-01-30",
                  "endDate": "2031-01-31"
                }
                """;

        String adjacentReservation = """
                {
                  "itemId": 1,
                  "startDate": "2031-01-29",
                  "endDate": "2031-01-30"
                }
                """;

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + renterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstReservation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + otherRenterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adjacentReservation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void ownerCanCancelAcceptedReservationAndRefundPayment() throws Exception {
        String renterToken = login("luis@test.com", "1234");
        String ownerToken = login("ana@test.com", "1234");

        String createReservationBody = """
                {
                  "itemId": 1,
                  "startDate": "2032-03-10",
                  "endDate": "2032-03-12"
                }
                """;

        String reservationResponse = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + renterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createReservationBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long reservationId = objectMapper.readTree(reservationResponse).get("id").asLong();

        mockMvc.perform(put("/api/reservations/" + reservationId + "/accept")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        mockMvc.perform(put("/api/reservations/" + reservationId + "/cancel")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        mockMvc.perform(get("/api/payments/me")
                        .header("Authorization", "Bearer " + renterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("REFUNDED"));
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
