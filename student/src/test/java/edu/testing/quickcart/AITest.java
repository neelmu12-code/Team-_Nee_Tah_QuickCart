package edu.testing.quickcart;

import edu.testing.quickcart.model.*;
import edu.testing.quickcart.service.PricingService;

import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.testing.quickcart.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

// Pre-existing weak candidate tests, retained for the AI experiment comparison.
class AITest {

    @Test
    void ordinaryCartReturnsPositiveTotal() {

        var quote = new PricingService().quote(
                List.of(item("10.00", 1)),
                CustomerTier.STANDARD,
                null,
                "ON",
                DATE
        );

        assertTrue(quote.total().signum() > 0);
    }

    @Test
    void goldCustomerPaysLessMerchandiseForOrdinaryCart() {

        var pricing = new PricingService();

        var cart = List.of(
                item("60.00", 1)
        );

        var standard = pricing.quote(
                cart,
                CustomerTier.STANDARD,
                null,
                "ON",
                DATE
        );

        var gold = pricing.quote(
                cart,
                CustomerTier.GOLD,
                null,
                "ON",
                DATE
        );

        assertTrue(
                gold.merchandise()
                        .compareTo(standard.merchandise()) < 0
        );
    }
}
