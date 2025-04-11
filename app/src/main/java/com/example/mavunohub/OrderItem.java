package com.example.mavunohub;

import java.util.List;

public class OrderItem {
    private String id;
    private String buyerId;
    private String farmerId;
    private List<Product> products;
    private String status;

    public OrderItem() {}

    public OrderItem(String id, String buyerId, String farmerId, List<Product> products, String status) {
        this.id = id;
        this.buyerId = buyerId;
        this.farmerId = farmerId;
        this.products = products;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalPrice() {
        double total = 0.0;
        for (Product product : products) {
            total += product.getPricePerUnit() * product.getQuantity();
        }
        return total;
    }

    public String getProductsDetails() {
        StringBuilder details = new StringBuilder();
        for (Product product : products) {
            details.append(product.getName())
                    .append(" (Qty: ").append(product.getQuantity())
                    .append(" @ KES ").append(String.format("%.2f", product.getPricePerUnit())).append(")\n");
        }
        return details.toString();
    }
}