package com.fiap.cheffyorderservice.application.services;

import com.fiap.cheffyorderservice.application.mappers.OrderStatusMapper;
import com.fiap.cheffyorderservice.application.ports.out.OrderRepositoryOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.PaymentOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.records.OrderStatusOutputRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PaymentStatusResponseRecord;
import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import com.fiap.cheffyorderservice.domain.exception.OrderNotFoundException;
import com.fiap.cheffyorderservice.domain.model.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderStatusServiceTest {

    @Mock private PaymentOutputPort paymentOutputPort;
    @Mock private OrderStatusMapper orderStatusMapper;
    @Mock private OrderRepositoryOutputPort orderRepository;

    @InjectMocks
    private OrderStatusService orderStatusService;

    @Test
    void shouldThrowOrderNotFoundExceptionWhenOrderDoesNotExist() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByOrderId(orderId.toString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderStatusService.checkOrderStatus(orderId))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessageContaining(orderId.toString());

        verify(paymentOutputPort, never()).getPaymentStatus(any());
    }

    @Test
    void shouldReturnStatusFromRepositoryWhenOrderIsPaid() {
        UUID orderId = UUID.randomUUID();
        Order paidOrder = Order.restore(orderId, BigDecimal.TEN, PaymentStatus.PAID, 1);
        when(orderRepository.findByOrderId(orderId.toString())).thenReturn(Optional.of(paidOrder));

        OrderStatusOutputRecord expected = new OrderStatusOutputRecord(orderId, PaymentStatus.PAID);
        when(orderStatusMapper.toOrderStatusOutputRecord(any())).thenReturn(expected);

        OrderStatusOutputRecord result = orderStatusService.checkOrderStatus(orderId);

        assertThat(result).isEqualTo(expected);
        verify(paymentOutputPort, never()).getPaymentStatus(any());
    }

    @Test
    void shouldCallPaymentApiWhenOrderIsNotPaid() {
        UUID orderId = UUID.randomUUID();
        Order pendingOrder = Order.restore(orderId, BigDecimal.TEN, PaymentStatus.PENDING, 1);
        when(orderRepository.findByOrderId(orderId.toString())).thenReturn(Optional.of(pendingOrder));

        PaymentStatusResponseRecord paymentResponse = new PaymentStatusResponseRecord(orderId.toString(), PaymentStatus.PENDING);
        when(paymentOutputPort.getPaymentStatus(orderId.toString())).thenReturn(paymentResponse);

        OrderStatusOutputRecord expected = new OrderStatusOutputRecord(orderId, PaymentStatus.PENDING);
        when(orderStatusMapper.toOrderStatusOutputRecord(paymentResponse)).thenReturn(expected);

        OrderStatusOutputRecord result = orderStatusService.checkOrderStatus(orderId);

        assertThat(result).isEqualTo(expected);
        verify(paymentOutputPort).getPaymentStatus(orderId.toString());
    }
}
