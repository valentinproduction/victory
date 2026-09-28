package com.example.victory;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.LocalDate;

public class Product {
    private long id;
    private String name;
    private double price;
    private String dueDate;
    @JsonCreator
    public Product() {
        this.dueDate = LocalDate.now().plusDays(5).toString();
    }
    public Product(long id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.dueDate = LocalDate.now().plusDays(5).toString();
    }

    
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public String getDueDate() {
        return dueDate;
    }
}
