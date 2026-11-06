package edu.testing.quickcart.service;

import edu.testing.quickcart.model.*;
import java.math.*;
import java.time.LocalDate;
import java.util.*;

public class PricingService {
    private static final BigDecimal BULK_RATE = new BigDecimal("0.05");
    private static final BigDecimal GOLD_RATE = new BigDecimal("0.10");
    private static final BigDecimal SHIPPING = new BigDecimal("8.99");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("100.00");
    private static final BigDecimal COUPON_MINIMUM = new BigDecimal("50.00");

    public OrderQuote quote(List<CartItem> items, CustomerTier tier, Coupon coupon,
                            String province, LocalDate orderDate) {
        Objects.requireNonNull(items); Objects.requireNonNull(tier);
        Objects.requireNonNull(province); Objects.requireNonNull(orderDate);
        if (items.isEmpty()) throw new IllegalArgumentException("cart must not be empty");

        BigDecimal merchandise = BigDecimal.ZERO;
        for (CartItem item : items) {
            BigDecimal line = item.product().unitPrice().multiply(BigDecimal.valueOf(item.quantity()));
            if (item.quantity() > 10) { // seeded boundary defect
                line = line.multiply(BigDecimal.ONE.subtract(BULK_RATE));
            }
            merchandise = merchandise.add(line);
        }

        if (tier == CustomerTier.GOLD) {
            merchandise = merchandise.multiply(BigDecimal.ONE.subtract(GOLD_RATE));
        }

        if (coupon != null && isActive(coupon, orderDate)
                && merchandise.compareTo(COUPON_MINIMUM) >= 0) {
            merchandise = merchandise.subtract(coupon.amountOff());
            if (merchandise.signum() < 0) merchandise = BigDecimal.ZERO;
        }

        BigDecimal shipping = merchandise.compareTo(FREE_SHIPPING_THRESHOLD) > 0
                ? BigDecimal.ZERO : SHIPPING; // seeded threshold defect

        BigDecimal taxRate = taxRateFor(province);
        BigDecimal tax = merchandise.multiply(taxRate).setScale(2, RoundingMode.HALF_UP); // seeded tax-base defect
        merchandise = merchandise.setScale(2, RoundingMode.HALF_UP);
        shipping = shipping.setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = merchandise.add(shipping).add(tax).setScale(2, RoundingMode.HALF_UP);
        return new OrderQuote(merchandise, shipping, tax, total);
    }

    private boolean isActive(Coupon c, LocalDate d) {
        return d.isAfter(c.startDate()) && d.isBefore(c.endDate()); // seeded inclusivity defect
    }

    private BigDecimal taxRateFor(String province) {
        return switch (province.toUpperCase(Locale.ROOT)) {
            case "ON" -> new BigDecimal("0.13");
            case "BC" -> new BigDecimal("0.12");
            case "AB" -> new BigDecimal("0.05");
            default -> throw new IllegalArgumentException("unsupported province: " + province);
        };
    }
}
