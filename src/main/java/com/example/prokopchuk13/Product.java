package com.example.prokopchuk13;

import java.io.Serializable;

public class Product implements Serializable {
    private String name;
    private String description;
    private String manufacturer;
    private int quantity;
    private double price;
    private int groupId;

    public Product(String name, String description, String manufacturer, int quantity, double price, int groupId) {
        this.name = name;
        this.description = description;
        this.manufacturer = manufacturer;
        this.quantity = quantity;
        this.price = price;
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public int getGroupId() {
        return groupId;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", groupId=" + groupId +
                '}';
    }
}

