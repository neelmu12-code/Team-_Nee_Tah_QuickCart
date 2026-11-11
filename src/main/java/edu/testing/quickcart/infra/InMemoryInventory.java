package edu.testing.quickcart.infra;
import edu.testing.quickcart.service.InventoryService;
import java.util.*;
public class InMemoryInventory implements InventoryService {
    private final Map<String,Integer> stock = new HashMap<>();
    public InMemoryInventory(Map<String,Integer> initial) { stock.putAll(initial); }
    public int available(String productId) { return stock.getOrDefault(productId, 0); }
    public void reserve(String productId, int quantity) {
        int current = available(productId);
        if (quantity <= 0 || current < quantity) throw new IllegalArgumentException("cannot reserve");
        stock.put(productId, current - quantity);
    }
}
