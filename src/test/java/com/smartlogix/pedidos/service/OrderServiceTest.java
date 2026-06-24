package com.smartlogix.pedidos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.smartlogix.pedidos.dto.OrderResponseDTO;
import com.smartlogix.pedidos.dto.ProductDTO;
import com.smartlogix.pedidos.model.Order;
import com.smartlogix.pedidos.model.ProductQuantity;
import com.smartlogix.pedidos.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "API_URL", "http://inventario/api/v1/products");
    }

    @Test
    void findAllWithProductsReturnsEmptyListWhenNoOrders() {
        when(orderRepository.findAll()).thenReturn(List.of());

        List<OrderResponseDTO> result = orderService.findAllWithProducts();

        assertTrue(result.isEmpty());
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(), any(org.springframework.core.ParameterizedTypeReference.class));
    }

    @Test
    void findAllWithProductsEnrichesOrdersWithProductData() {
        Order order = sampleOrder(1L, "Juan Perez", "PENDIENTE", List.of(new ProductQuantity(10L, 2L)));
        when(orderRepository.findAll()).thenReturn(List.of(order));
        ProductDTO product = sampleProduct(10L, "Notebook");
        mockInventoryResponse(List.of(product));

        List<OrderResponseDTO> result = orderService.findAllWithProducts();

        assertEquals(1, result.size());
        OrderResponseDTO dto = result.get(0);
        assertEquals(1L, dto.getId());
        assertEquals(1, dto.getProductList().size());
        assertEquals("Notebook", dto.getProductList().get(0).getProduct().getName());
        assertEquals(2L, dto.getProductList().get(0).getQuantity());
    }

    @Test
    void findByIdWithProductsReturnsEmptyWhenMissing() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<OrderResponseDTO> result = orderService.findByIdWithProducts(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdWithProductsReturnsDtoWhenFound() {
        Order order = sampleOrder(2L, "Ana Diaz", "EN_PROCESO", List.of(new ProductQuantity(20L, 1L)));
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
        mockInventoryResponse(List.of(sampleProduct(20L, "Mouse")));

        Optional<OrderResponseDTO> result = orderService.findByIdWithProducts(2L);

        assertTrue(result.isPresent());
        assertEquals("Ana Diaz", result.get().getCustomerName());
    }

    @Test
    void findByStatusWithProductsDelegatesToRepository() {
        Order order = sampleOrder(3L, "Cliente C", "COMPLETADO", List.of());
        when(orderRepository.findByStatus("COMPLETADO")).thenReturn(List.of(order));

        List<OrderResponseDTO> result = orderService.findByStatusWithProducts("COMPLETADO");

        assertEquals(1, result.size());
        assertEquals("COMPLETADO", result.get(0).getStatus());
        verify(orderRepository).findByStatus("COMPLETADO");
    }

    @Test
    void createOrderDelegatesToRepository() {
        Order order = sampleOrder(null, "Nuevo Cliente", "PENDIENTE", List.of());
        Order saved = sampleOrder(4L, "Nuevo Cliente", "PENDIENTE", List.of());
        when(orderRepository.save(order)).thenReturn(saved);

        Order result = orderService.createOrder(order);

        assertSame(saved, result);
        verify(orderRepository).save(order);
    }

    @Test
    void updateStatusUpdatesWhenOrderExists() {
        Order order = sampleOrder(5L, "Cliente E", "PENDIENTE", List.of());
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.updateStatus(5L, "COMPLETADO");

        assertEquals("COMPLETADO", result.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void updateStatusThrowsWhenOrderDoesNotExist() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.updateStatus(404L, "CANCELADO"));

        assertEquals("Pedido con ID 404 no encontrado.", ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void deleteOrderDeletesWhenExists() {
        when(orderRepository.existsById(6L)).thenReturn(true);

        orderService.deleteOrder(6L);

        verify(orderRepository).deleteById(6L);
    }

    @Test
    void deleteOrderThrowsWhenMissing() {
        when(orderRepository.existsById(404L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.deleteOrder(404L));

        assertEquals("Pedido con ID 404 no encontrado.", ex.getMessage());
        verify(orderRepository, never()).deleteById(404L);
    }

    @SuppressWarnings("unchecked")
    private void mockInventoryResponse(List<ProductDTO> products) {
        ResponseEntity<List<ProductDTO>> response = new ResponseEntity<>(products, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(null),
                any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(response);
    }

    private Order sampleOrder(Long id, String customerName, String status, List<ProductQuantity> products) {
        return new Order(id, customerName, status, LocalDateTime.now(), products);
    }

    private ProductDTO sampleProduct(Long id, String name) {
        ProductDTO dto = new ProductDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setSku("SKU-" + id);
        dto.setStock(100);
        return dto;
    }
}
