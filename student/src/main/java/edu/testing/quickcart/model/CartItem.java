package edu.testing.quickcart.model;
import java.util.Objects;
public record CartItem(Product product, int quantity) {
    public CartItem {
        Objects.requireNonNull(product);
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
    }
}
