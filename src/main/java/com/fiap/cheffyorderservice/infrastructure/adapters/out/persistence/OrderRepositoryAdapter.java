package com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence;

import com.fiap.cheffyorderservice.application.ports.out.OrderRepositoryOutputPort;
import com.fiap.cheffyorderservice.domain.model.Order;
import com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.document.OrderDocument;
import com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.mapper.OrderPersistenceMapper;
import com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.repository.OrderMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}
