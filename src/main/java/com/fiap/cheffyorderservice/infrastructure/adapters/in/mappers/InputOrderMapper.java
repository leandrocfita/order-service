package com.fiap.cheffyorderservice.infrastructure.adapters.in.mappers;

import com.fiap.cheffyorderservice.application.ports.in.records.PlaceOrderCommandRecord;
import com.fiap.cheffyorderservice.infrastructure.adapters.in.records.InputOrderRecord;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InputOrderMapper {

    PlaceOrderCommandRecord toCommand(InputOrderRecord input);
}
