package com.fiap.cheffyorderservice.application.ports.out;

import com.fiap.cheffyorderservice.application.ports.out.records.ReprocessOrderOutputRecord;

public interface ReprocessOrderOutputPort {
    void publishReprocessOrderEvent(ReprocessOrderOutputRecord reprocessOrderOutputRecord);
}
