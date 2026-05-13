package com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.repository;

import com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.document.OrderDocument;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface OrderMongoRepository extends MongoRepository<OrderDocument, String> {
}
