package com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;


@Data
@NoArgsConstructor
@Document(collection = "orders")
public class OrderDocument {
    @Id
    private String id = UUID.randomUUID().toString();
    private String orderId;
    private Double totalAmount;
    private String status;
}
