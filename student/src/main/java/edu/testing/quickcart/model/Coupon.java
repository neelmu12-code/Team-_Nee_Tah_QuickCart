package edu.testing.quickcart.model;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
public record Coupon(String code, BigDecimal amountOff, LocalDate startDate, LocalDate endDate) {
    public Coupon {
        Objects.requireNonNull(code); Objects.requireNonNull(amountOff); Objects.requireNonNull(startDate); Objects.requireNonNull(endDate);
        if (code.isBlank()) throw new IllegalArgumentException("code must be nonblank");
        if (amountOff.signum() < 0) throw new IllegalArgumentException("amountOff must be nonnegative");
        if (endDate.isBefore(startDate)) throw new IllegalArgumentException("endDate before startDate");
    }
}
