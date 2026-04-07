package com.smartlogix.pedidos.controller;

import com.smartlogix.pedidos.dto.OrderDTO;
import com.smartlogix.pedidos.model.Order;
import com.smartlogix.pedidos.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // GET /api/v1/orders
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.findAll();
        if (!orders.isEmpty()) {
            return new ResponseEntity<>(orders, HttpStatus.OK);
        }
        return new ResponseEntity<>(orders, HttpStatus.NO_CONTENT);
    }

    // GET /api/v1/orders/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Optional<Order> optional = orderService.findById(id);
        if (optional.isPresent()) {
            return new ResponseEntity<>(optional.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // GET /api/v1/orders/status/{status}
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status) {
        List<Order> orders = orderService.findByStatus(status);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    // GET /api/v1/orders/branch/{branchId}
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<Order>> getOrdersByBranch(@PathVariable Long branchId) {
        List<Order> orders = orderService.findByBranchId(branchId);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    // POST /api/v1/orders
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderDTO orderDTO) {
        try {
            Order savedOrder = orderService.createOrder(orderDTO);
            return new ResponseEntity<>(savedOrder, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // PATCH /api/v1/orders/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Order updated = orderService.updateStatus(id, status);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // DELETE /api/v1/orders/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // ===================== ENDPOINTS QUE CONSULTAN EL INVENTARIO =====================

    // GET /api/v1/orders/inventory/products
    @GetMapping("/inventory/products")
    public ResponseEntity<?> getInventoryProducts() {
        try {
            return new ResponseEntity<>(orderService.getAvailableProducts(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("No se pudo conectar con la API de inventario: " + e.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    // GET /api/v1/orders/inventory/branches
    @GetMapping("/inventory/branches")
    public ResponseEntity<?> getInventoryBranches() {
        try {
            return new ResponseEntity<>(orderService.getAvailableBranches(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("No se pudo conectar con la API de inventario: " + e.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
