package edu.testing.quickcart.service;
public interface InventoryService {
    int available(String productId);
    void reserve(String productId, int quantity);
}
