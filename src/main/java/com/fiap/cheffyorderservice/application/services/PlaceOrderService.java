package com.fiap.cheffyorderservice.application.services;

import com.fiap.cheffyorderservice.application.mappers.PlaceOrderMapper;
import com.fiap.cheffyorderservice.application.ports.in.PlaceOrderInputPort;
import com.fiap.cheffyorderservice.application.ports.in.records.PlaceOrderCommandRecord;
import com.fiap.cheffyorderservice.application.ports.out.OrderRepositoryOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.PaymentOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.records.PaymentRequestRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PaymentStatusResponseRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PlaceOrderOutputRecord;
import com.fiap.cheffyorderservice.domain.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaceOrderService implements PlaceOrderInputPort {
    private final PaymentOutputPort paymentOutputPort;
    private final OrderRepositoryOutputPort orderRepository;
    private final PlaceOrderMapper placeOrderMapper;

    @Override
    public PlaceOrderOutputRecord execute(PlaceOrderCommandRecord request) {
        Order order = Order.create(
                request.orderId(),
                request.totalAmount()
        );

        Optional<Order> orderOptional = orderRepository.findByOrderId(request.orderId().toString());

        if (orderOptional.isPresent()) {
            log.warn("Order already exists, using existing record instead of creating a new one [orderId={}]", request.orderId());
            order = orderOptional.get();
        } else {
            log.info("Creating new order record [orderId={}]", request.orderId());
            orderRepository.save(order);
        }

        log.info("Requesting payment for order [orderId={}, amount={}]", request.orderId(), request.totalAmount());

        paymentOutputPort.requestPayment(new PaymentRequestRecord(
                request.totalAmount().intValue(),
                request.orderId().toString(),
                request.orderId().toString()
        ));

        PaymentStatusResponseRecord paymentStatus = paymentOutputPort.getPaymentStatus(request.orderId().toString());
        log.info("Payment status received [orderId={}, status={}]", request.orderId(), paymentStatus.status());
        order.updateStatus(paymentStatus.status());
        order = orderRepository.updateByOrderId(request.orderId().toString(), order);
        return placeOrderMapper.toPlaceOrderOutput(order);
    }
}
