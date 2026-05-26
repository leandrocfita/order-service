package com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence;

import com.fiap.cheffyorderservice.application.ports.out.OrderRepositoryOutputPort;
import com.fiap.cheffyorderservice.domain.model.Order;
import com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.document.OrderDocument;
import com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.mapper.OrderPersistenceMapper;
import com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.repository.OrderMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepositoryOutputPort {
    private final OrderMongoRepository mongoRepository;
    private final OrderPersistenceMapper mapper;

    @Override
    public Order save(Order order) {
       OrderDocument document = mongoRepository.save(mapper.toDocument(order));
       return mapper.toDomain(document);
    }

    @Override
    public Optional<Order> findByOrderId(String orderId) {
        return mongoRepository.findByOrderId(orderId).map(mapper::toDomain);
    }

    @Override
    public Order updateByOrderId(String orderId, Order order) {
        Optional<OrderDocument> existingDocument = mongoRepository.findByOrderId(orderId);

        if (existingDocument.isPresent()) {
            OrderDocument document = existingDocument.get();
            document.setTotalAmount(order.getTotalAmount());
            document.setStatus(order.getStatus());
            document.setProcessingAttempts(order.getProcessingAttempts());

            OrderDocument updatedDocument = mongoRepository.save(document);
            Order domain = mapper.toDomain(updatedDocument);
            return domain;
        }

        throw new IllegalArgumentException("Order with orderId " + orderId + " not found");
    }
}
