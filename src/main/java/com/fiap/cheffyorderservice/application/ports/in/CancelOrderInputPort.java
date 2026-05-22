package com.fiap.cheffyorderservice.application.ports.in;

public interface CancelOrderInputPort {
    void cancelOrder(String orderId);
}
