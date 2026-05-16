package com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.repository;

import com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.document.OrderDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface OrderMongoRepository extends MongoRepository<OrderDocument, String> {
    Optional<OrderDocument> findByOrderId(String orderId);
}
