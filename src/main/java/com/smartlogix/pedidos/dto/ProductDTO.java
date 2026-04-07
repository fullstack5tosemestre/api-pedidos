package com.smartlogix.pedidos.dto;

public class ProductDTO {
    private Long id;
    private String name;
    private String sku;
    private int stock;
    private WarehouseDTO inWarehouse;

    public ProductDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public WarehouseDTO getInWarehouse() { return inWarehouse; }
    public void setInWarehouse(WarehouseDTO inWarehouse) { this.inWarehouse = inWarehouse; }
}
