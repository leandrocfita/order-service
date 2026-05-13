package com.fiap.cheffyorderservice.application.mappers;

import com.fiap.cheffyorderservice.application.ports.out.records.PlaceOrderOutputRecord;
import com.fiap.cheffyorderservice.domain.model.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlaceOrderMapper {
    PlaceOrderOutputRecord toPlaceOrderOutput(Order order);
}
