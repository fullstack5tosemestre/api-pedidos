package com.smartlogix.pedidos.service;

import com.smartlogix.pedidos.client.InventoryClient;
import com.smartlogix.pedidos.dto.BranchDTO;
import com.smartlogix.pedidos.dto.OrderDTO;
import com.smartlogix.pedidos.dto.OrderItemDTO;
import com.smartlogix.pedidos.dto.ProductDTO;
import com.smartlogix.pedidos.model.Order;
import com.smartlogix.pedidos.model.OrderItem;
import com.smartlogix.pedidos.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InventoryClient inventoryClient;

    // ===================== CRUD =====================

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> findByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> findByBranchId(Long branchId) {
        return orderRepository.findByBranchId(branchId);
    }

    /**
     * Crea un pedido nuevo.
     * Consulta la API de inventario para validar que existan el producto y la sucursal,
     * y obtiene el nombre y SKU del producto para guardarlo localmente.
     */
    public Order createOrder(OrderDTO dto) {
        // Validar que la sucursal existe en el inventario
        BranchDTO branch = inventoryClient.getBranchById(dto.getBranchId());
        if (branch == null) {
            throw new RuntimeException("La sucursal con ID " + dto.getBranchId() + " no existe en el inventario.");
        }

        // Construir el objeto Order
        Order order = new Order();
        order.setCustomerName(dto.getCustomerName());
        order.setBranchId(dto.getBranchId());
        order.setStatus("PENDIENTE");

        // Construir los OrderItems consultando el inventario
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemDTO itemDTO : dto.getItems()) {
            ProductDTO product = inventoryClient.getProductById(itemDTO.getProductId());
            if (product == null) {
                throw new RuntimeException("El producto con ID " + itemDTO.getProductId() + " no existe en el inventario.");
            }

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductSku(product.getSku());
            item.setQuantity(itemDTO.getQuantity());
            item.setOrder(order);
            items.add(item);
        }

        order.setItems(items);
        return orderRepository.save(order);
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

    // ===================== CONSULTAS AL INVENTARIO =====================

    /**
     * Devuelve todos los productos disponibles en la API de inventario.
     */
    public Object getAvailableProducts() {
        return inventoryClient.getAllProducts();
    }

    /**
     * Devuelve todas las sucursales disponibles en la API de inventario.
     */
    public Object getAvailableBranches() {
        return inventoryClient.getAllBranches();
    }
}
