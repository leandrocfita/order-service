package com.fiap.cheffyorderservice.infrastructure.adapters.in.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.cheffyorderservice.application.ports.in.OrderStatusInputPort;
import com.fiap.cheffyorderservice.application.ports.in.PlaceOrderInputPort;
import com.fiap.cheffyorderservice.application.ports.in.records.PlaceOrderCommandRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.OrderStatusOutputRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PlaceOrderOutputRecord;
import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import com.fiap.cheffyorderservice.infrastructure.adapters.in.mappers.InputOrderMapper;
import com.fiap.cheffyorderservice.infrastructure.adapters.in.records.InputOrderRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private PlaceOrderInputPort placeOrderInputPort;
    @MockBean private InputOrderMapper inputOrderMapper;
    @MockBean private OrderStatusInputPort orderStatusInputPort;
    @MockBean private JwtDecoder jwtDecoder;

    @Test
    void shouldPlaceOrderAndReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        InputOrderRecord input = new InputOrderRecord(orderId, BigDecimal.valueOf(100));
        PlaceOrderCommandRecord command = new PlaceOrderCommandRecord(orderId, BigDecimal.valueOf(100));
        PlaceOrderOutputRecord output = new PlaceOrderOutputRecord(orderId, PaymentStatus.CREATED);

        when(inputOrderMapper.toCommand(any())).thenReturn(command);
        when(placeOrderInputPort.execute(command)).thenReturn(output);

        mockMvc.perform(post("/v1/order")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(orderId.toString()))
            .andExpect(jsonPath("$.status").value("criado"));
    }

    @Test
    void shouldGetOrderStatusAndReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderStatusOutputRecord statusOutput = new OrderStatusOutputRecord(orderId, PaymentStatus.PAID);

        when(orderStatusInputPort.checkOrderStatus(orderId)).thenReturn(statusOutput);

        mockMvc.perform(get("/v1/order")
                .with(jwt())
                .param("orderId", orderId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(orderId.toString()))
            .andExpect(jsonPath("$.status").value("pago"));
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/v1/order")
                .param("orderId", UUID.randomUUID().toString()))
            .andExpect(status().isUnauthorized());
    }
}
