package com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.mapper;

import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import com.fiap.cheffyorderservice.domain.model.Order;
import com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.document.OrderDocument;
import com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.mapper.OrderPersistenceMapperImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderPersistenceMapperTest {

    private final OrderPersistenceMapper mapper = new OrderPersistenceMapperImpl();

    @Test
    void shouldMapOrderDocumentToDomain() {
        UUID orderId = UUID.randomUUID();
        OrderDocument doc = new OrderDocument();
        doc.setOrderId(orderId.toString());
        doc.setTotalAmount(BigDecimal.valueOf(100));
        doc.setStatus(PaymentStatus.CREATED);
        doc.setProcessingAttempts(2);

        Order result = mapper.toDomain(doc);

        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(result.getProcessingAttempts()).isEqualTo(2);
    }

    @Test
    void shouldMapOrderToDocument() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.restore(orderId, BigDecimal.valueOf(200), PaymentStatus.PAID, 3);

        OrderDocument result = mapper.toDocument(order);

        assertThat(result.getOrderId()).isEqualTo(orderId.toString());
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(result.getProcessingAttempts()).isEqualTo(3);
    }
}
