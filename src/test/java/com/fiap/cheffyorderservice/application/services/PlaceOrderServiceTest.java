package com.fiap.cheffyorderservice.application.services;

import com.fiap.cheffyorderservice.application.mappers.PlaceOrderMapper;
import com.fiap.cheffyorderservice.application.ports.in.records.PlaceOrderCommandRecord;
import com.fiap.cheffyorderservice.application.ports.out.OrderRepositoryOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.OrderStatusChangeOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.PaymentOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.ReprocessOrderOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.records.PaymentStatusResponseRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PlaceOrderOutputRecord;
import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import com.fiap.cheffyorderservice.domain.exception.PaymentServiceException;
import com.fiap.cheffyorderservice.domain.exception.PaymentTimeoutException;
import com.fiap.cheffyorderservice.domain.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaceOrderServiceTest {

    @Mock private PaymentOutputPort paymentOutputPort;
    @Mock private OrderRepositoryOutputPort orderRepository;
    @Mock private PlaceOrderMapper placeOrderMapper;
    @Mock private OrderStatusChangeOutputPort orderStatusChangeOutputPort;
    @Mock private ReprocessOrderOutputPort reprocessOrderOutputPort;

    @InjectMocks
    private PlaceOrderService placeOrderService;

    private UUID orderId;
    private BigDecimal totalAmount;
    private PlaceOrderCommandRecord command;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        totalAmount = BigDecimal.valueOf(100);
        command = new PlaceOrderCommandRecord(orderId, totalAmount);
    }

    @Test
    void shouldCreateNewOrderAndProcessPaymentSuccessfully() {
        when(orderRepository.findByOrderId(orderId.toString())).thenReturn(Optional.empty());

        Order orderAfterFirst = Order.restore(orderId, totalAmount, PaymentStatus.SENT_TO_PAYMENT_GATEWAY, 1);
        Order orderAfterSecond = Order.restore(orderId, totalAmount, PaymentStatus.PAID, 1);
        when(orderRepository.updateByOrderId(eq(orderId.toString()), any()))
            .thenReturn(orderAfterFirst)
            .thenReturn(orderAfterSecond);

        when(paymentOutputPort.getPaymentStatus(orderId.toString()))
            .thenReturn(new PaymentStatusResponseRecord(orderId.toString(), PaymentStatus.PAID));

        PlaceOrderOutputRecord expected = new PlaceOrderOutputRecord(orderId, PaymentStatus.PAID);
        when(placeOrderMapper.toPlaceOrderOutput(any(Order.class))).thenReturn(expected);

        PlaceOrderOutputRecord result = placeOrderService.execute(command);

        assertThat(result).isEqualTo(expected);
        verify(orderRepository).save(any(Order.class));
        verify(paymentOutputPort).requestPayment(any());
        verify(orderRepository, times(2)).updateByOrderId(eq(orderId.toString()), any());
        verify(orderStatusChangeOutputPort, times(2)).publishStatusChangeEvent(any());
    }

    @Test
    void shouldReprocessExistingOrderWithoutSaving() {
        Order existing = Order.restore(orderId, totalAmount, PaymentStatus.PENDING, 1);
        when(orderRepository.findByOrderId(orderId.toString())).thenReturn(Optional.of(existing));

        Order afterFirst = Order.restore(orderId, totalAmount, PaymentStatus.SENT_TO_PAYMENT_GATEWAY, 2);
        Order afterSecond = Order.restore(orderId, totalAmount, PaymentStatus.PAID, 2);
        when(orderRepository.updateByOrderId(eq(orderId.toString()), any()))
            .thenReturn(afterFirst)
            .thenReturn(afterSecond);

        when(paymentOutputPort.getPaymentStatus(orderId.toString()))
            .thenReturn(new PaymentStatusResponseRecord(orderId.toString(), PaymentStatus.PAID));

        when(placeOrderMapper.toPlaceOrderOutput(any(Order.class)))
            .thenReturn(new PlaceOrderOutputRecord(orderId, PaymentStatus.PAID));

        placeOrderService.execute(command);

        verify(orderRepository, never()).save(any());
        verify(paymentOutputPort).requestPayment(any());
    }

    @Test
    void shouldSkipPaymentWhenOrderAlreadyHasFinalStatus() {
        Order existing = Order.restore(orderId, totalAmount, PaymentStatus.PAID, 1);
        when(orderRepository.findByOrderId(orderId.toString())).thenReturn(Optional.of(existing));
        when(placeOrderMapper.toPlaceOrderOutput(existing))
            .thenReturn(new PlaceOrderOutputRecord(orderId, PaymentStatus.PAID));

        placeOrderService.execute(command);

        verify(paymentOutputPort, never()).requestPayment(any());
        verify(orderRepository, never()).updateByOrderId(any(), any());
    }

    @Test
    void shouldSkipPaymentWhenOrderIsSentToGateway() {
        Order existing = Order.restore(orderId, totalAmount, PaymentStatus.SENT_TO_PAYMENT_GATEWAY, 1);
        when(orderRepository.findByOrderId(orderId.toString())).thenReturn(Optional.of(existing));
        when(placeOrderMapper.toPlaceOrderOutput(existing))
            .thenReturn(new PlaceOrderOutputRecord(orderId, PaymentStatus.SENT_TO_PAYMENT_GATEWAY));

        placeOrderService.execute(command);

        verify(paymentOutputPort, never()).requestPayment(any());
    }

    @Test
    void shouldSetPendingAndPublishReprocessOnPaymentException() {
        when(orderRepository.findByOrderId(orderId.toString())).thenReturn(Optional.empty());
        doThrow(new RuntimeException("payment failed")).when(paymentOutputPort).requestPayment(any());

        Order pendingOrder = Order.restore(orderId, totalAmount, PaymentStatus.PENDING, 1);
        when(orderRepository.updateByOrderId(eq(orderId.toString()), any())).thenReturn(pendingOrder);

        when(placeOrderMapper.toPlaceOrderOutput(any(Order.class)))
            .thenReturn(new PlaceOrderOutputRecord(orderId, PaymentStatus.PENDING));

        placeOrderService.execute(command);

        verify(reprocessOrderOutputPort).publishReprocessOrderEvent(any());
        verify(orderStatusChangeOutputPort).publishStatusChangeEvent(any());
    }

    @Test
    void shouldHandlePaymentTimeoutExceptionGracefully() {
        when(orderRepository.findByOrderId(orderId.toString())).thenReturn(Optional.empty());
        doThrow(new PaymentTimeoutException("timeout")).when(paymentOutputPort).requestPayment(any());

        Order pendingOrder = Order.restore(orderId, totalAmount, PaymentStatus.PENDING, 1);
        when(orderRepository.updateByOrderId(eq(orderId.toString()), any())).thenReturn(pendingOrder);

        when(placeOrderMapper.toPlaceOrderOutput(any(Order.class)))
            .thenReturn(new PlaceOrderOutputRecord(orderId, PaymentStatus.PENDING));

        placeOrderService.execute(command);

        verify(reprocessOrderOutputPort).publishReprocessOrderEvent(any());
    }

    @Test
    void shouldHandlePaymentServiceExceptionGracefully() {
        when(orderRepository.findByOrderId(orderId.toString())).thenReturn(Optional.empty());
        doThrow(new PaymentServiceException("service error", 503)).when(paymentOutputPort).requestPayment(any());

        Order pendingOrder = Order.restore(orderId, totalAmount, PaymentStatus.PENDING, 1);
        when(orderRepository.updateByOrderId(eq(orderId.toString()), any())).thenReturn(pendingOrder);

        when(placeOrderMapper.toPlaceOrderOutput(any(Order.class)))
            .thenReturn(new PlaceOrderOutputRecord(orderId, PaymentStatus.PENDING));

        placeOrderService.execute(command);

        verify(reprocessOrderOutputPort).publishReprocessOrderEvent(any());
    }
}
