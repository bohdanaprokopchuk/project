package com.example.prokopchuk13;

import java.io.Serializable;

public class ProductGroup implements Serializable {
    private String name;
    private String description;

    public ProductGroup(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "ProductGroup{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
