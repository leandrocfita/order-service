package com.fiap.cheffyorderservice.application.ports.in;

import com.fiap.cheffyorderservice.application.ports.in.records.PlaceOrderCommandRecord;

import java.util.UUID;

public interface PlaceOrderInputPort {
    UUID execute(PlaceOrderCommandRecord request);
}
