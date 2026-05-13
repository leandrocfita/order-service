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

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaceOrderService implements PlaceOrderInputPort {
    private final PaymentOutputPort paymentOutputPort;
    private final OrderRepositoryOutputPort orderRepository;
    private final PlaceOrderMapper placeOrderMapper;

    @Override
    public PlaceOrderOutputRecord execute(PlaceOrderCommandRecord request) {
        log.info("PlaceOrderService.execute - START - orderId: [{}]", request.orderId());

        paymentOutputPort.requestPayment(new PaymentRequestRecord(
                request.totalAmount().intValue(),
                request.orderId().toString(),
                request.orderId().toString()
        ));

        PaymentStatusResponseRecord paymentStatus = paymentOutputPort.getPaymentStatus(request.orderId().toString());

        Order order = Order.create(
                request.orderId(),
                request.totalAmount(),
                paymentStatus.status()
        );

        orderRepository.save(order);

        log.info("PlaceOrderService.execute - END - orderId: [{}], status: [{}]",
                order.getOrderId(), order.getStatus()
        );

        return placeOrderMapper.toPlaceOrderOutput(order);
    }
}
