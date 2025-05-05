package com.example.mavunohub;

import com.google.firebase.Timestamp;

public class Product {
    private String id;
    private String productId;
    private String name;
    private String description;
    private String imageUrl;
    private double pricePerUnit;
    private int quantity;
    private String stockStatus;
    private String phone;
    private String county;
    private String subCounty;
    private String sellerId;
    private String unit;
    private String sellerName;
    private String userId;
    private double totalPrice;
    private Timestamp addedAt;

    public Product() {}

    public Product(String id, String productId, String name, String description, String imageUrl, double pricePerUnit,
                   int quantity, String stockStatus, String phone, String county, String subCounty, String sellerId,
                   String unit, String sellerName, String userId, Timestamp addedAt) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.pricePerUnit = pricePerUnit;
        this.quantity = quantity;
        this.stockStatus = stockStatus;
        this.phone = phone;
        this.county = county;
        this.subCounty = subCounty;
        this.sellerId = sellerId;
        this.unit = unit;
        this.sellerName = sellerName;
        this.userId = userId;
        this.totalPrice = pricePerUnit * quantity;
        this.addedAt = addedAt;
    }

    public String getId() { return id; }
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public double getPricePerUnit() { return pricePerUnit; }
    public int getQuantity() { return quantity; }
    public String getStockStatus() { return stockStatus; }
    public String getPhone() { return phone; }
    public String getCounty() { return county; }
    public String getSubCounty() { return subCounty; }
    public String getSellerId() { return sellerId; }
    public String getUnit() { return unit; }
    public String getSellerName() { return sellerName; }
    public String getUserId() { return userId; }
    public double getTotalPrice() { return totalPrice; }
    public Timestamp getAddedAt() { return addedAt; }

    public void setId(String id) { this.id = id; }
    public void setProductId(String productId) { this.productId = productId; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
        updateTotalPrice();
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        updateTotalPrice();
    }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setCounty(String county) { this.county = county; }
    public void setSubCounty(String subCounty) { this.subCounty = subCounty; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setAddedAt(Timestamp addedAt) { this.addedAt = addedAt; }

    public boolean isInStock() {
        return "In Stock".equalsIgnoreCase(this.stockStatus);
    }

    private void updateTotalPrice() {
        this.totalPrice = this.pricePerUnit * this.quantity;
    }
}
