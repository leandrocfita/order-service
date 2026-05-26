package com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.document;

import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.UUID;


@Data
@NoArgsConstructor
@Document(collection = "orders")
public class OrderDocument {
    @Id
    private String orderId;
    private BigDecimal totalAmount;
    private PaymentStatus status;
    private int processingAttempts;
}
