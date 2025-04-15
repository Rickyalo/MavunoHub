package com.example.mavunohub;

import java.util.List;

public class OrderItem {
    private String id;
    private String phone; // Shared phone for identifying buyer or farmer
    private String userType; // "Buyer" or "Farmer"
    private List<Product> products;
    private String status;

    public OrderItem() {}

    public OrderItem(String id, String phone, String userType, List<Product> products, String status) {
        this.id = id;
        this.phone = phone;
        this.userType = userType;
        this.products = products;
        this.status = status;
    }

    // Getter and Setter for ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter and Setter for Phone
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Getter and Setter for User Type
    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    // Getter and Setter for Products
    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    // Getter and Setter for Status
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Calculate Total Price
    public double getTotalPrice() {
        double total = 0.0;
        for (Product product : products) {
            total += product.getPricePerUnit() * product.getQuantity();
        }
        return total;
    }

    // Generate Product Details as String
    public String getProductsDetails() {
        StringBuilder details = new StringBuilder();
        for (Product product : products) {
            details.append(product.getName())
                    .append(" (Qty: ").append(product.getQuantity())
                    .append(" @ KES ").append(String.format("%.2f", product.getPricePerUnit()))
                    .append(")\n");
        }
        return details.toString();
    }
}