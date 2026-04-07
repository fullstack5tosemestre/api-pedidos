package com.smartlogix.pedidos.dto;

import java.util.List;

public class OrderDTO {
    private String customerName;
    private Long branchId;
    private List<OrderItemDTO> items;

    public OrderDTO() {}

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }

    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
}
