package edu.testing.quickcart;

import edu.testing.quickcart.infra.InMemoryInventory;
import edu.testing.quickcart.model.*;
import edu.testing.quickcart.service.*;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static edu.testing.quickcart.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

class OrderIntegrationTest {

    @Test
    void acceptedOrderReducesInventoryAndProducesQuote() {

        var inventory = new InMemoryInventory(
                Map.of("SKU-1", 5)
        );

        var paymentCalls = new AtomicInteger();

        PaymentGateway fakePayment = (customer, amount) -> {
            paymentCalls.incrementAndGet();

            return "C-1".equals(customer)
                    && amount.signum() > 0;
        };

        var checkout = new OrderService(
                new PricingService(),
                inventory,
                fakePayment
        );

        var result = checkout.placeOrder(
                "C-1",
                List.of(item("200.00", 2)),
                CustomerTier.STANDARD,
                null,
                "ON",
                DATE
        );

        assertEquals(
                OrderResult.Status.ACCEPTED,
                result.status()
        );

        money("400.00", result.quote().merchandise());

        assertEquals(3, inventory.available("SKU-1"));
        assertEquals(1, paymentCalls.get());
    }

    @Test
    void oneOutOfStockLinePreservesAllInventoriesAndDoesNotCharge() {

        var inventory = new InMemoryInventory(
                Map.of(
                        "SKU-1", 5,
                        "SKU-2", 0
                )
        );

        var paymentCalls = new AtomicInteger();

        PaymentGateway fakePayment = (customer, amount) -> {
            paymentCalls.incrementAndGet();
            return true;
        };

        var checkout = new OrderService(
                new PricingService(),
                inventory,
                fakePayment
        );

        var other = new Product(
                "SKU-2",
                "Other",
                new BigDecimal("200.00")
        );

        var result = checkout.placeOrder(
                "C-1",
                List.of(
                        item("200.00", 1),
                        new CartItem(other, 1)
                ),
                CustomerTier.STANDARD,
                null,
                "ON",
                DATE
        );

        assertEquals(
                OrderResult.Status.OUT_OF_STOCK,
                result.status()
        );

        assertEquals(5, inventory.available("SKU-1"));
        assertEquals(0, inventory.available("SKU-2"));
        assertEquals(0, paymentCalls.get());
    }
}