package com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence;

import com.fiap.cheffyorderservice.domain.enums.PaymentStatus;
import com.fiap.cheffyorderservice.domain.model.Order;
import com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.document.OrderDocument;
import com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.mapper.OrderPersistenceMapper;
import com.fiap.cheffyorderservice.infrastructure.adapters.out.persistence.repository.OrderMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderRepositoryAdapterTest {

    @Mock private OrderMongoRepository mongoRepository;
    @Mock private OrderPersistenceMapper mapper;

    @InjectMocks
    private OrderRepositoryAdapter adapter;

    private OrderDocument buildDocument(UUID orderId) {
        OrderDocument doc = new OrderDocument();
        doc.setOrderId(orderId.toString());
        doc.setTotalAmount(BigDecimal.TEN);
        doc.setStatus(PaymentStatus.CREATED);
        doc.setProcessingAttempts(1);
        return doc;
    }

    @Test
    void saveShouldPersistAndReturnDomainOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.create(orderId, BigDecimal.TEN);
        OrderDocument doc = buildDocument(orderId);

        when(mapper.toDocument(order)).thenReturn(doc);
        when(mongoRepository.save(doc)).thenReturn(doc);
        when(mapper.toDomain(doc)).thenReturn(order);

        Order result = adapter.save(order);

        assertThat(result).isEqualTo(order);
        verify(mongoRepository).save(doc);
    }

    @Test
    void findByOrderIdShouldReturnOrderWhenFound() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.create(orderId, BigDecimal.TEN);
        OrderDocument doc = buildDocument(orderId);

        when(mongoRepository.findByOrderId(orderId.toString())).thenReturn(Optional.of(doc));
        when(mapper.toDomain(doc)).thenReturn(order);

        Optional<Order> result = adapter.findByOrderId(orderId.toString());

        assertThat(result).isPresent().contains(order);
    }

    @Test
    void findByOrderIdShouldReturnEmptyWhenNotFound() {
        String orderId = UUID.randomUUID().toString();
        when(mongoRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        Optional<Order> result = adapter.findByOrderId(orderId);

        assertThat(result).isEmpty();
    }

    @Test
    void updateByOrderIdShouldUpdateAndReturnOrder() {
        UUID orderId = UUID.randomUUID();
        Order updatedOrder = Order.restore(orderId, BigDecimal.TEN, PaymentStatus.PAID, 1);
        OrderDocument existingDoc = buildDocument(orderId);
        OrderDocument savedDoc = buildDocument(orderId);

        when(mongoRepository.findByOrderId(orderId.toString())).thenReturn(Optional.of(existingDoc));
        when(mongoRepository.save(existingDoc)).thenReturn(savedDoc);
        when(mapper.toDomain(savedDoc)).thenReturn(updatedOrder);

        Order result = adapter.updateByOrderId(orderId.toString(), updatedOrder);

        assertThat(result).isEqualTo(updatedOrder);
        verify(mongoRepository).save(existingDoc);
    }

    @Test
    void updateByOrderIdShouldThrowWhenOrderNotFound() {
        String orderId = UUID.randomUUID().toString();
        Order order = Order.create(UUID.fromString(orderId), BigDecimal.TEN);

        when(mongoRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.updateByOrderId(orderId, order))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(orderId);
    }
}
