package com.fiap.cheffyorderservice.application.services;

import com.fiap.cheffyorderservice.application.mappers.PlaceOrderMapper;
import com.fiap.cheffyorderservice.application.ports.in.PlaceOrderInputPort;
import com.fiap.cheffyorderservice.application.ports.in.records.PlaceOrderCommandRecord;
import com.fiap.cheffyorderservice.application.ports.out.OrderRepositoryOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.OrderStatusChangeOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.PaymentOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.records.OrderStatusOutputRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PaymentRequestRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PaymentStatusResponseRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PlaceOrderOutputRecord;
import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
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
    private final OrderStatusChangeOutputPort orderStatusChangeOutputPort;

    @Override
    public PlaceOrderOutputRecord execute(PlaceOrderCommandRecord request) {
        log.info("PlaceOrderService.execute - START - orderId: [{}]", request.orderId());

        Order order = Order.create(
                request.orderId(),
                request.totalAmount()
        );

        Optional<Order> orderOptional = orderRepository.findByOrderId(request.orderId().toString());

        if (orderOptional.isPresent()) {
            log.warn("PlaceOrderService.execute - Order with orderId [{}] already exists. Skipping creation.",
                    request.orderId()
            );

            order = orderOptional.get();
        } else {
            orderRepository.save(order);
        }

        if(order.getStatus() != PaymentStatus.PENDING){
            log.info(
                    "Pedido [{}] já processado com status [{}]",
                    order.getOrderId(),
                    order.getStatus()
            );

            return placeOrderMapper.toPlaceOrderOutput(order);
        }

        paymentOutputPort.requestPayment(new PaymentRequestRecord(
                request.totalAmount().intValue(),
                request.orderId().toString(),
                request.orderId().toString()
        ));

        order.updateStatus(PaymentStatus.SENT_TO_PAYMENT_GATEWAY);
        order = orderRepository.updateByOrderId(request.orderId().toString(), order);

        PaymentStatusResponseRecord paymentStatus = paymentOutputPort.getPaymentStatus(request.orderId().toString());

        order.updateStatus(paymentStatus.status());
        order = orderRepository.updateByOrderId(request.orderId().toString(), order);

        log.info("PlaceOrderService.execute - END - orderId: [{}], status: [{}]",
                order.getOrderId(), order.getStatus()
        );

        orderStatusChangeOutputPort.publishStatusChangeEvent(
                new OrderStatusOutputRecord(
                        order.getOrderId(),
                        order.getStatus()
                )
        );

        return placeOrderMapper.toPlaceOrderOutput(order);
    }
}
