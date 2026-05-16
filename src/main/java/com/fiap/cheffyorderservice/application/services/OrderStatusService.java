package com.fiap.cheffyorderservice.application.services;

import com.fiap.cheffyorderservice.application.mappers.OrderStatusMapper;
import com.fiap.cheffyorderservice.application.ports.in.OrderStatusInputPort;
import com.fiap.cheffyorderservice.application.ports.out.OrderRepositoryOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.PaymentOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.records.OrderStatusOutputRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PaymentStatusResponseRecord;
import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import com.fiap.cheffyorderservice.domain.exception.OrderNotFoundException;
import com.fiap.cheffyorderservice.domain.model.Order;
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
    private final OrderRepositoryOutputPort orderRepository;

    @Override
    public OrderStatusOutputRecord checkOrderStatus(UUID orderId) {
        String orderIdString = orderId.toString();

        Order order = orderRepository.findByOrderId(orderIdString).orElse(null);

        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        PaymentStatusResponseRecord paymentStatusResponseRecord = order.getStatus().equals(PaymentStatus.PAID)
                ? new PaymentStatusResponseRecord(order.getOrderId().toString(), order.getStatus())
                : paymentOutputPort.getPaymentStatus(orderIdString);

        return orderStatusMapper.toOrderStatusOutputRecord(paymentStatusResponseRecord);
    }
}
