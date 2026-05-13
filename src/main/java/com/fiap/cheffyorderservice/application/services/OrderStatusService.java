package com.fiap.cheffyorderservice.application.services;

import com.fiap.cheffyorderservice.application.mappers.OrderStatusMapper;
import com.fiap.cheffyorderservice.application.ports.in.OrderStatusInputPort;
import com.fiap.cheffyorderservice.application.ports.out.PaymentOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.records.OrderStatusOutputRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PaymentStatusResponseRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderStatusService implements OrderStatusInputPort {
    private final PaymentOutputPort paymentOutputPort;
    private final OrderStatusMapper orderStatusMapper;

    @Override
    public OrderStatusOutputRecord checkOrderStatus(UUID orderId) {
        return orderStatusMapper.toOrderStatusOutputRecord(
                paymentOutputPort.getPaymentStatus(orderId.toString())
        );
    }
}
