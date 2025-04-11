package com.example.mavunohub;

public class CartItem {
    private String id;
    private String name;
    private double pricePerUnit;
    private int quantity;
    private String imageUrl;
    private String county;
    private String subCounty;
    private String phone;
    private String userId;

    public CartItem() {}

    public CartItem(String id, String name, double pricePerUnit, int quantity, String imageUrl, String county, String subCounty, String phone, String userId) {
        this.id = id;
        this.name = name;
        this.pricePerUnit = pricePerUnit;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.county = county;
        this.subCounty = subCounty;
        this.phone = phone;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getSubCounty() {
        return subCounty;
    }

    public void setSubCounty(String subCounty) {
        this.subCounty = subCounty;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getTotalPrice() {
        return pricePerUnit * quantity;
    }

    // Additional Validation Method for Phone Number
    public String getFormattedPhone() {
        if (phone != null && phone.startsWith("0")) {
            return "+254" + phone.substring(1); // Add country code for Kenyan numbers
        }
        return phone;
    }
}