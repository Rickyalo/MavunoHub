package com.example.mavunohub;

public class Product {
    private String id;
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
    private double totalPrice;
    private String userId;

    public Product() {}

    public Product(String id, String name, String description, String imageUrl, double pricePerUnit, int quantity,
                   String stockStatus, String phone, String county, String subCounty, String sellerId, String unit,
                   String sellerName, String userId) {
        this.id = id;
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
        this.totalPrice = pricePerUnit * quantity;
        this.userId = userId;
    }

    // Existing Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getPrice() {
        return pricePerUnit;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public String getPhone() {
        return phone;
    }

    public String getCounty() {
        return county;
    }

    public String getSubCounty() {
        return subCounty;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getUnit() {
        return unit;
    }

    public String getSellerName() {
        return sellerName;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getUserId() {
        return userId;
    }

    // Existing Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
        this.totalPrice = this.pricePerUnit * this.quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = this.pricePerUnit * this.quantity;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public void setSubCounty(String subCounty) {
        this.subCounty = subCounty;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // New Functionality - Added Enhancements
    public boolean isInStock() {
        return "In Stock".equalsIgnoreCase(this.stockStatus);
    }

    public void updateTotalPrice() {
        this.totalPrice = this.pricePerUnit * this.quantity;
    }
}