package com.fiap.cheffyorderservice.application.services;

import com.fiap.cheffyorderservice.application.mappers.PlaceOrderMapper;
import com.fiap.cheffyorderservice.application.ports.in.PlaceOrderInputPort;
import com.fiap.cheffyorderservice.application.ports.in.records.PlaceOrderCommandRecord;
import com.fiap.cheffyorderservice.application.ports.in.records.ReprocessOrderCommandRecord;
import com.fiap.cheffyorderservice.application.ports.out.OrderRepositoryOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.OrderStatusChangeOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.PaymentOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.ReprocessOrderOutputPort;
import com.fiap.cheffyorderservice.application.ports.out.records.*;
import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import com.fiap.cheffyorderservice.domain.exception.PaymentServiceException;
import com.fiap.cheffyorderservice.domain.exception.PaymentTimeoutException;
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
    private final ReprocessOrderOutputPort reprocessOrderOutputPort;

    @Override
    public PlaceOrderOutputRecord execute(PlaceOrderCommandRecord request) {
        log.info("PlaceOrderService.execute - START - orderId: [{}]", request.orderId());

        Optional<Order> orderOptional = orderRepository.findByOrderId(request.orderId().toString());

        Order order = Order.create(
                request.orderId(),
                request.totalAmount()
        );

        if (orderOptional.isPresent()) {

            order = orderOptional.get();
            order.incrementProcessingAttempts();

            log.warn("PlaceOrderService.execute - Order with orderId [{}] already exists. Reprocess Attempt: [{}] - Skipping creation.",
                    request.orderId(),
                    orderOptional.get().getProcessingAttempts()
            );
        } else {
            log.info("Creating new order record [orderId={}]", request.orderId());
            orderRepository.save(order);
        }

        if((order.getStatus() != PaymentStatus.PENDING) && (order.getStatus() != PaymentStatus.CREATED)){
            log.info(
                    "Order [{}] already processed with status [{}] - Skipping payment request.",
                    order.getOrderId(),
                    order.getStatus()
            );

            return placeOrderMapper.toPlaceOrderOutput(order);
        }


            try {

                log.info("Requesting payment for order [orderId={}, amount={}]", request.orderId(), request.totalAmount());
                paymentOutputPort.requestPayment(new PaymentRequestRecord(
                        request.totalAmount().intValue(),
                        request.orderId().toString(),
                        request.orderId().toString()
                ));

                order.updateStatus(PaymentStatus.SENT_TO_PAYMENT_GATEWAY);

                order = orderRepository.updateByOrderId(request.orderId().toString(), order);

                log.info("Order sent to payment gateway [orderId={}, status={}]", request.orderId(), order.getStatus());

                publishStatusChangeEvent(order);

                PaymentStatusResponseRecord paymentStatus = paymentOutputPort.getPaymentStatus(request.orderId().toString());
                log.info("Payment status received [orderId={}, status={}]", request.orderId(), paymentStatus.status());

                order.updateStatus(paymentStatus.status());
                order = orderRepository.updateByOrderId(request.orderId().toString(), order);

                log.info("PlaceOrderService.execute - END - orderId: [{}], status: [{}]",
                        order.getOrderId(), order.getStatus()
                );

                publishStatusChangeEvent(order);

                return placeOrderMapper.toPlaceOrderOutput(order);

            } catch (Exception ex) {

                log.error(
                        "Payment service unavailable. Sending order [{}] to pending flow",
                        request.orderId(),
                        ex
                );

                order.updateStatus(PaymentStatus.PENDING);

                order = orderRepository.updateByOrderId(
                        request.orderId().toString(),
                        order
                );

                publishStatusChangeEvent(order);

                reprocessOrderOutputPort.publishReprocessOrderEvent(new ReprocessOrderOutputRecord(
                        order.getOrderId(),
                        order.getStatus(),
                        order.getTotalAmount(),
                        order.getProcessingAttempts()
                ));

                return placeOrderMapper.toPlaceOrderOutput(order);
            }

    }

    private void publishStatusChangeEvent(Order order) {
        orderStatusChangeOutputPort.publishStatusChangeEvent(
                new OrderStatusOutputRecord(
                        order.getOrderId(),
                        order.getStatus()
                )
        );
    }
}
