package com.smartlogix.pedidos.service;

import com.smartlogix.pedidos.dto.OrderProductDTO;
import com.smartlogix.pedidos.dto.OrderResponseDTO;
import com.smartlogix.pedidos.dto.ProductDTO;
import com.smartlogix.pedidos.model.Order;
import com.smartlogix.pedidos.model.ProductQuantity;
import com.smartlogix.pedidos.repository.OrderRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    // call inventory API to get all products based on id list
    @Value("${inventory.api.base-url}")
    private String API_URL;

    public List<ProductDTO> getOrderProductsFromAPI(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(API_URL + "/by-id/").queryParam("ids", ids);

        ResponseEntity<List<ProductDTO>> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, null,
                new ParameterizedTypeReference<List<ProductDTO>>() {

                });

        return response.getBody() == null ? Collections.emptyList() : response.getBody();
    }

    // ===================== CRUD =====================

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public List<OrderResponseDTO> findAllWithProducts() {
        return enrichOrdersWithProducts(orderRepository.findAll());
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<OrderResponseDTO> findByIdWithProducts(Long id) {
        return orderRepository.findById(id).map(this::enrichOrderWithProducts);
    }

    public List<Order> findByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public List<OrderResponseDTO> findByStatusWithProducts(String status) {
        return enrichOrdersWithProducts(orderRepository.findByStatus(status));
    }

    public Order createOrder(Order o) {
        return orderRepository.save(o);
    }

    /**
     * Actualiza el estado de un pedido.
     * Estados validos: PENDIENTE, EN_PROCESO, COMPLETADO, CANCELADO
     */
    public Order updateStatus(Long id, String newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido con ID " + id + " no encontrado."));
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Pedido con ID " + id + " no encontrado.");
        }
        orderRepository.deleteById(id);
    }

    private List<OrderResponseDTO> enrichOrdersWithProducts(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> productIds = orders.stream()
                .filter(order -> order.getProductList() != null)
                .flatMap(order -> order.getProductList().stream())
                .map(ProductQuantity::getProductId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, ProductDTO> productMap = buildProductMap(new java.util.ArrayList<>(productIds));

        return orders.stream()
                .map(order -> mapToOrderResponse(order, productMap))
                .toList();
    }

    private OrderResponseDTO enrichOrderWithProducts(Order order) {
        if (order == null) {
            return null;
        }

        List<Long> productIds = order.getProductList() == null
                ? Collections.emptyList()
                : order.getProductList().stream()
                        .map(ProductQuantity::getProductId)
                        .filter(id -> id != null)
                        .distinct()
                        .toList();

        Map<Long, ProductDTO> productMap = buildProductMap(productIds);
        return mapToOrderResponse(order, productMap);
    }

    private Map<Long, ProductDTO> buildProductMap(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ProductDTO> products = getOrderProductsFromAPI(productIds);
        Map<Long, ProductDTO> productMap = new HashMap<>();
        for (ProductDTO product : products) {
            if (product != null && product.getId() != null) {
                productMap.put(product.getId(), product);
            }
        }
        return productMap;
    }

    private OrderResponseDTO mapToOrderResponse(Order order, Map<Long, ProductDTO> productMap) {
        List<OrderProductDTO> orderProducts = order.getProductList() == null
                ? Collections.emptyList()
                : order.getProductList().stream()
                        .map(productQuantity -> new OrderProductDTO(
                                productQuantity.getProductId(),
                                productQuantity.getQuantity(),
                                productMap.get(productQuantity.getProductId())))
                        .toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getCustomerName(),
                order.getStatus(),
                order.getCreatedAt(),
                orderProducts);
    }

}