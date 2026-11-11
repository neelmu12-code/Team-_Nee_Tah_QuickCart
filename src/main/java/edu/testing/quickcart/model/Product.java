package edu.testing.quickcart.model;
import java.math.BigDecimal;
import java.util.Objects;
public record Product(String id, String name, BigDecimal unitPrice) {
    public Product {
        Objects.requireNonNull(id); Objects.requireNonNull(name); Objects.requireNonNull(unitPrice);
        if (id.isBlank() || name.isBlank()) throw new IllegalArgumentException("id/name must be nonblank");
        if (unitPrice.signum() < 0) throw new IllegalArgumentException("price must be nonnegative");
    }
}
