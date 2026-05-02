package com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.mapper;

import com.fiap.cheffyorderservice.domain.model.Order;
import com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.document.OrderDocument;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderPersistenceMapper {
    OrderDocument toDocument(Order order);
    Order toDomain(OrderDocument document);

    @ObjectFactory
    default Order createOrder(OrderDocument document) {
        return Order.create(
                UUID.fromString(document.getOrderId()),
                document.getTotalAmount(),
                document.getCurrency(),
                document.getStatus()
        );
    }
}
