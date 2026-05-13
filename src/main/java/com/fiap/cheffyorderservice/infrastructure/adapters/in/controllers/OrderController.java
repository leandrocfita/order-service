package com.fiap.cheffyorderservice.infrastructure.adapters.in.controllers;

import com.fiap.cheffyorderservice.application.ports.in.OrderStatusInputPort;
import com.fiap.cheffyorderservice.application.ports.in.PlaceOrderInputPort;
import com.fiap.cheffyorderservice.application.ports.out.records.OrderStatusOutputRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PlaceOrderOutputRecord;
import com.fiap.cheffyorderservice.infrastructure.adapters.in.mappers.InputOrderMapper;
import com.fiap.cheffyorderservice.infrastructure.adapters.in.records.InputOrderRecord;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/order")
public class OrderController {
    private final PlaceOrderInputPort placeOrderInputPort;
    private final InputOrderMapper inputOrderMapper;
    private final OrderStatusInputPort orderStatusInputPort;

    @PostMapping
    public ResponseEntity<PlaceOrderOutputRecord> placeOrder(@RequestBody @Valid InputOrderRecord request) {
        log.info("OrderController.placeOrder - START - Place order: [{}]", request.orderId());

        var response = placeOrderInputPort.execute(inputOrderMapper.toCommand(request));

        log.info("OrderController.placeOrder - END - Order placed successfully");

        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<OrderStatusOutputRecord> getOrderStatus(@RequestParam UUID orderId) {
        log.info("OrderController.getOrderStatus - START - Get order status for orderId: [{}]", orderId);

        var response = orderStatusInputPort.checkOrderStatus(orderId);

        log.info("OrderController.getOrderStatus - END - Order status retrieved successfully");

        return ResponseEntity.ok().body(response);
    }
}
