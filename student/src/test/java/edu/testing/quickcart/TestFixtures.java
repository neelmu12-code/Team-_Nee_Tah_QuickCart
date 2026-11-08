package edu.testing.quickcart;

import edu.testing.quickcart.model.*;
import edu.testing.quickcart.service.PricingService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TestFixtures {

    static final LocalDate DATE = LocalDate.of(2026, 11, 8);

    private TestFixtures() { }

    static Product product(String price) {
        return new Product(
                "SKU-1",
                "Test item",
                new BigDecimal(price)
        );
    }

    static CartItem item(String price, int quantity) {
        return new CartItem(
                product(price),
                quantity
        );
    }

    static Coupon coupon(
            String amount,
            LocalDate start,
            LocalDate end) {

        return new Coupon(
                "SAVE",
                new BigDecimal(amount),
                start,
                end
        );
    }

    static OrderQuote quote(String price, int quantity) {
        return new PricingService().quote(
                List.of(item(price, quantity)),
                CustomerTier.STANDARD,
                null,
                "ON",
                DATE
        );
    }

    static void money(String expected, BigDecimal actual) {
        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual),
                () -> "Expected $" + expected + " but was $" + actual
        );
    }
}