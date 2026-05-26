package com.fiap.cheffyorderservice.application.mappers;

import com.fiap.cheffyorderservice.application.ports.out.records.OrderStatusOutputRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PaymentStatusResponseRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderStatusMapper {
    @Mapping(target = "orderId", expression = "java(mapStringToUUID(paymentStatusResponseRecord.paymentId()))")
    OrderStatusOutputRecord toOrderStatusOutputRecord(PaymentStatusResponseRecord paymentStatusResponseRecord);

    default UUID mapStringToUUID(String value) {
        return UUID.fromString(value);
    }
}
