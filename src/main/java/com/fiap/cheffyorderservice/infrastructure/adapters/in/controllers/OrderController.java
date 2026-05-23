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
        log.info("HTTP request received to place order [orderId={}, totalAmount={}]", request.orderId(), request.totalAmount());
        var response = placeOrderInputPort.execute(inputOrderMapper.toCommand(request));
        log.info("Order placed successfully [orderId={}]", response.orderId());
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<OrderStatusOutputRecord> getOrderStatus(@RequestParam UUID orderId) {
        log.info("HTTP request received to get order status [orderId={}]", orderId);
        var response = orderStatusInputPort.checkOrderStatus(orderId);
        log.info("Order status retrieved successfully [orderId={}, paymentStatus={}]", response.orderId(), response.status());
        return ResponseEntity.ok().body(response);
    }
}
