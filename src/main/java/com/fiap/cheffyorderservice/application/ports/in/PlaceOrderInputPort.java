package com.fiap.cheffyorderservice.application.ports.in;

import com.fiap.cheffyorderservice.application.ports.in.records.PlaceOrderCommandRecord;
import com.fiap.cheffyorderservice.application.ports.out.records.PlaceOrderOutputRecord;

public interface PlaceOrderInputPort {
    PlaceOrderOutputRecord execute(PlaceOrderCommandRecord request);
}
