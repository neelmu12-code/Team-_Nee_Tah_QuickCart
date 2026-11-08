package edu.testing.quickcart;

import edu.testing.quickcart.infra.InMemoryInventory;
import edu.testing.quickcart.model.*;
import edu.testing.quickcart.service.*;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static edu.testing.quickcart.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("known-defect")
class KnownDefectsTest {

    private final PricingService pricing = new PricingService();

    @Test
    void bulkDiscountMustStartAtQuantityTen() {

        var q = pricing.quote(
                List.of(item("10.00", 10)),
                CustomerTier.STANDARD,
                null,
                "ON",
                DATE
        );

        // 10 x $10 = $100
        // 5% bulk discount = $95
        money("95.00", q.merchandise());
    }

    @Test
    void shippingMustBeFreeAtExactlyOneHundred() {

        var q = quote("100.00", 1);

        // Specification: free shipping >= $100
        money("0.00", q.shipping());
    }

    @ParameterizedTest
    @CsvSource({"ON, 2.47", "BC, 2.28", "AB, 0.95"})
    void taxMustIncludeShipping(String province, String expectedTax) {
        var q = pricing.quote(List.of(item("10.00", 1)), CustomerTier.STANDARD,
                null, province, DATE);
        money(expectedTax, q.tax());
    }

    @Test
    void singleDayCouponIsValidOnItsOnlyDate() {
        var c = coupon("5.00", DATE, DATE);
        var q = pricing.quote(List.of(item("60.00", 1)), CustomerTier.STANDARD,
                c, "ON", DATE);
        money("55.00", q.merchandise());
    }

    @Test
    void couponMustBeValidOnStartDate() {

        var c = coupon(
                "5.00",
                DATE,
                DATE.plusDays(2)
        );

        var q = pricing.quote(
                List.of(item("60.00", 1)),
                CustomerTier.STANDARD,
                c,
                "ON",
                DATE
        );

        money("55.00", q.merchandise());
    }

    @Test
    void couponMustBeValidOnEndDate() {

        var c = coupon(
                "5.00",
                DATE.minusDays(2),
                DATE
        );

        var q = pricing.quote(
                List.of(item("60.00", 1)),
                CustomerTier.STANDARD,
                c,
                "ON",
                DATE
        );

        money("55.00", q.merchandise());
    }

    @Test
    void exactStockIsEnoughForAnOrder() {

        var inventory = new InMemoryInventory(
                Map.of("SKU-1", 3)
        );

        PaymentGateway payment = mock(
                PaymentGateway.class
        );

        when(payment.authorize(
                anyString(),
                any(BigDecimal.class)
        )).thenReturn(true);

        var checkout = new OrderService(
                pricing,
                inventory,
                payment
        );

        var result = checkout.placeOrder(
                "C-1",
                List.of(item("200.00", 3)),
                CustomerTier.STANDARD,
                null,
                "ON",
                DATE
        );

        assertEquals(
                OrderResult.Status.ACCEPTED,
                result.status()
        );

        assertEquals(
                0,
                inventory.available("SKU-1")
        );

        verify(payment).authorize(
                eq("C-1"),
                any(BigDecimal.class)
        );
    }

    @Test
    void declinedPaymentMustNotReduceInventory() {

        var inventory = new InMemoryInventory(
                Map.of("SKU-1", 5)
        );

        PaymentGateway payment =
                (customer, amount) -> false;

        var checkout = new OrderService(
                pricing,
                inventory,
                payment
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
                OrderResult.Status.PAYMENT_DECLINED,
                result.status()
        );

        // Payment declined: inventory must stay at 5
        assertEquals(
                5,
                inventory.available("SKU-1")
        );
    }

    @Test
    void authorizationMustHappenBeforeInventoryReservation() {

        InventoryService inventory = mock(
                InventoryService.class
        );

        PaymentGateway payment = mock(
                PaymentGateway.class
        );

        when(inventory.available("SKU-1"))
                .thenReturn(5);

        when(payment.authorize(
                anyString(),
                any(BigDecimal.class)
        )).thenReturn(true);

        var checkout = new OrderService(
                pricing,
                inventory,
                payment
        );

        checkout.placeOrder(
                "C-1",
                List.of(item("200.00", 2)),
                CustomerTier.STANDARD,
                null,
                "ON",
                DATE
        );

        InOrder order = inOrder(
                payment,
                inventory
        );

        // Payment must be authorized first
        order.verify(payment).authorize(
                eq("C-1"),
                any(BigDecimal.class)
        );

        // Inventory can only be reserved afterward
        order.verify(inventory).reserve(
                "SKU-1",
                2
        );
    }
}
