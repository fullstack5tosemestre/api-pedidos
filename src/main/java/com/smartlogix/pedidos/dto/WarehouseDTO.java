package com.smartlogix.pedidos.dto;

public class WarehouseDTO {
    private Long id;
    private String name;
    private BranchDTO inBranch;

    public WarehouseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BranchDTO getInBranch() { return inBranch; }
    public void setInBranch(BranchDTO inBranch) { this.inBranch = inBranch; }
}
