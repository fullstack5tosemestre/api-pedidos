package com.smartlogix.pedidos.client;

import com.smartlogix.pedidos.dto.BranchDTO;
import com.smartlogix.pedidos.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * Cliente HTTP que se comunica con la API de Inventario del companero.
 * Usa WebClient (reactivo) para hacer las llamadas.
 *
 * La URL base se configura en application.properties:
 *   inventory.api.base-url=http://localhost:8080
 * Cuando el companero suba su API a la nube, cambias esa URL por la IP publica.
 */
@Component
public class InventoryClient {

    private final WebClient webClient;

    public InventoryClient(@Value("${inventory.api.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    // ===================== PRODUCTOS =====================

    /**
     * Obtiene todos los productos del inventario.
     */
    public List<ProductDTO> getAllProducts() {
        return webClient.get()
                .uri("/api/v1/products")
                .retrieve()
                .bodyToFlux(ProductDTO.class)
                .collectList()
                .block();
    }

    /**
     * Obtiene un producto por su ID.
     * Retorna null si no existe (404).
     */
    public ProductDTO getProductById(Long id) {
        try {
            return webClient.get()
                    .uri("/api/v1/products/{id}", id)
                    .retrieve()
                    .bodyToMono(ProductDTO.class)
                    .block();
        } catch (Exception e) {
            return null;
        }
    }

    // ===================== SUCURSALES (BRANCHES) =====================

    /**
     * Obtiene todas las sucursales del inventario.
     */
    public List<BranchDTO> getAllBranches() {
        return webClient.get()
                .uri("/api/v1/branches")
                .retrieve()
                .bodyToFlux(BranchDTO.class)
                .collectList()
                .block();
    }

    /**
     * Obtiene una sucursal por su ID.
     * Retorna null si no existe (404).
     */
    public BranchDTO getBranchById(Long id) {
        try {
            return webClient.get()
                    .uri("/api/v1/branches/{id}", id)
                    .retrieve()
                    .bodyToMono(BranchDTO.class)
                    .block();
        } catch (Exception e) {
            return null;
        }
    }
}
