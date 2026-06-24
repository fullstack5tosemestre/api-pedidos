package com.smartlogix.pedidos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.pedidos.dto.OrderResponseDTO;
import com.smartlogix.pedidos.model.Order;
import com.smartlogix.pedidos.service.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    @Test
    void getAllOrdersReturnsOkWhenListIsNotEmpty() throws Exception {
        OrderResponseDTO dto = sampleResponse(1L, "Juan Perez", "PENDIENTE");
        when(orderService.findAllWithProducts()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].customerName").value("Juan Perez"));
    }

    @Test
    void getAllOrdersReturnsNoContentWhenListIsEmpty() throws Exception {
        when(orderService.findAllWithProducts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getOrderByIdReturnsOkWhenFound() throws Exception {
        OrderResponseDTO dto = sampleResponse(5L, "Ana Diaz", "EN_PROCESO");
        when(orderService.findByIdWithProducts(5L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/v1/orders/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.status").value("EN_PROCESO"));
    }

    @Test
    void getOrderByIdReturnsNotFoundWhenMissing() throws Exception {
        when(orderService.findByIdWithProducts(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/orders/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrdersByStatusReturnsOk() throws Exception {
        OrderResponseDTO dto = sampleResponse(7L, "Cliente C", "COMPLETADO");
        when(orderService.findByStatusWithProducts("COMPLETADO")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/orders/status/{status}", "COMPLETADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("COMPLETADO"));
    }

    @Test
    void createOrderReturnsCreated() throws Exception {
        Order request = new Order(null, "Nuevo Cliente", "PENDIENTE", LocalDateTime.now(), List.of());
        Order saved = new Order(10L, "Nuevo Cliente", "PENDIENTE", request.getCreatedAt(), List.of());
        when(orderService.createOrder(any(Order.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void createOrderReturnsBadRequestWhenServiceThrows() throws Exception {
        Order request = new Order(null, "Cliente Invalido", "PENDIENTE", LocalDateTime.now(), List.of());
        when(orderService.createOrder(any(Order.class))).thenThrow(new RuntimeException("datos invalidos"));

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatusReturnsOkWhenFound() throws Exception {
        Order updated = new Order(1L, "Juan Perez", "COMPLETADO", LocalDateTime.now(), List.of());
        when(orderService.updateStatus(eq(1L), eq("COMPLETADO"))).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/orders/{id}/status", 1L).param("status", "COMPLETADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETADO"));
    }

    @Test
    void updateStatusReturnsNotFoundWhenServiceThrows() throws Exception {
        when(orderService.updateStatus(eq(404L), eq("COMPLETADO")))
                .thenThrow(new RuntimeException("Pedido con ID 404 no encontrado."));

        mockMvc.perform(patch("/api/v1/orders/{id}/status", 404L).param("status", "COMPLETADO"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOrderReturnsNoContentWhenFound() throws Exception {
        mockMvc.perform(delete("/api/v1/orders/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(orderService).deleteOrder(1L);
    }

    @Test
    void deleteOrderReturnsNotFoundWhenServiceThrows() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Pedido con ID 404 no encontrado."))
                .when(orderService).deleteOrder(404L);

        mockMvc.perform(delete("/api/v1/orders/{id}", 404L))
                .andExpect(status().isNotFound());
    }

    private OrderResponseDTO sampleResponse(Long id, String customerName, String status) {
        return new OrderResponseDTO(id, customerName, status, LocalDateTime.now(), List.of());
    }
}
