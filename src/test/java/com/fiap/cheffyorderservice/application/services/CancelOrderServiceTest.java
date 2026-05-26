package com.fiap.cheffyorderservice.application.services;

import com.fiap.cheffyorderservice.application.ports.out.OrderRepositoryOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.OrderStatusChangeOutputPort;
import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import com.fiap.cheffyorderservice.domain.model.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelOrderServiceTest {

    @Mock private OrderRepositoryOutputPort orderRepository;
    @Mock private OrderStatusChangeOutputPort orderStatusChangeOutputPort;

    @InjectMocks
    private CancelOrderService cancelOrderService;

    @Test
    void shouldReturnSilentlyWhenOrderNotFound() {
        String orderId = UUID.randomUUID().toString();
        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        cancelOrderService.cancelOrder(orderId);

        verify(orderRepository, never()).updateByOrderId(any(), any());
        verify(orderStatusChangeOutputPort, never()).publishStatusChangeEvent(any());
    }

    @Test
    void shouldReturnSilentlyWhenOrderAlreadyCanceled() {
        String orderId = UUID.randomUUID().toString();
        Order canceled = Order.restore(UUID.fromString(orderId), BigDecimal.TEN, PaymentStatus.CANCELED, 1);
        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(canceled));

        cancelOrderService.cancelOrder(orderId);

        verify(orderRepository, never()).updateByOrderId(any(), any());
        verify(orderStatusChangeOutputPort, never()).publishStatusChangeEvent(any());
    }

    @Test
    void shouldCancelOrderAndPublishEvent() {
        UUID uuid = UUID.randomUUID();
        String orderId = uuid.toString();
        Order active = Order.restore(uuid, BigDecimal.TEN, PaymentStatus.PENDING, 1);
        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(active));
        when(orderRepository.updateByOrderId(eq(orderId), any())).thenReturn(active);

        cancelOrderService.cancelOrder(orderId);

        verify(orderRepository).updateByOrderId(eq(orderId), any());
        verify(orderStatusChangeOutputPort).publishStatusChangeEvent(any());
    }
}
