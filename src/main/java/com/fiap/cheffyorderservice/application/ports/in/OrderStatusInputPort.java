package com.fiap.cheffyorderservice.application.ports.in;

import com.fiap.cheffyorderservice.application.ports.out.records.OrderStatusOutputRecord;

import java.util.UUID;

public interface OrderStatusInputPort {
    OrderStatusOutputRecord checkOrderStatus(UUID orderId);
}
