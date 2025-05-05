package com.example.mavunohub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderItem {
    private String id;
    private String buyerId;
    private String farmerId;
    private List<Map<String, Object>> products;
    private String status;
    private Double totalPrice;
    private String phone;

    public OrderItem() {
        this.products = new ArrayList<>();
        this.totalPrice = null;
    }

    public OrderItem(String id, String buyerId, String farmerId, List<Map<String, Object>> products, String status,
                     Double totalPrice, String phone) {
        this.id = id;
        this.buyerId = buyerId;
        this.farmerId = farmerId;
        this.products = (products != null) ? products : new ArrayList<>();
        this.status = status;
        this.totalPrice = totalPrice;
        this.phone = phone;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

    public String getFarmerId() { return farmerId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }

    public List<Map<String, Object>> getProducts() { return products; }
    public void setProducts(List<Map<String, Object>> products) { this.products = products; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public void setImageUrl(String imageUrl) { }

    public void setQuantity(int quantity) { }
}