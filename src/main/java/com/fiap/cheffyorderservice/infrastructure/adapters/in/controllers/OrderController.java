package com.fiap.cheffyorderservice.infrastructure.adapters.in.controllers;

import com.fiap.cheffyorderservice.application.ports.in.PlaceOrderInputPort;
import com.fiap.cheffyorderservice.infrastructure.adapters.in.mappers.InputOrderMapper;
import com.fiap.cheffyorderservice.infrastructure.adapters.in.records.InputOrderRecord;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/order")
public class OrderController {
    private final PlaceOrderInputPort placeOrderInputPort;
    private final InputOrderMapper inputOrderMapper;

    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestBody @Valid InputOrderRecord request) {
        log.info("OrderController.placeOrder - START - Place order: [{}]", request.orderId());

        var response = placeOrderInputPort.execute(inputOrderMapper.toCommand(request));

        log.info("OrderController.placeOrder - END - Order placed successfully");

        return ResponseEntity.ok().body(response.toString());
    }
}
