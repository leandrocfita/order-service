package com.fiap.cheffyorderservice.application.ports.out;

import com.fiap.cheffyorderservice.application.ports.out.records.OrderStatusOutputRecord;

public interface OrderStatusChangeOutputPort {

    void publishStatusChangeEvent(OrderStatusOutputRecord orderStatusOutputRecord);
}
