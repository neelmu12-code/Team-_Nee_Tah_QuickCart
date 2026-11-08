package edu.testing.quickcart;

import edu.testing.quickcart.infra.InMemoryInventory;
import edu.testing.quickcart.model.*;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static edu.testing.quickcart.TestFixtures.*;

class ModelAndInventoryTest {

    @Test
    void productRejectsNegativePriceAndBlankIdentity() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new Product(
                                "x", "item", new BigDecimal("-0.01")
                        )
                ),

                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new Product(
                                " ", "item", BigDecimal.ZERO
                        )
                ),

                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new Product(
                                "x", " ", BigDecimal.ZERO
                        )
                )
        );
    }

    @Test
    void productPermitsZeroPrice() {
        var p = new Product(
                "FREE",
                "Sample",
                BigDecimal.ZERO
        );

        money("0", p.unitPrice());
    }

    @Test
    void cartItemRequiresPositiveQuantityAndNonNullProduct() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new CartItem(product("1.00"), 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new CartItem(product("1.00"), -1)
        );

        assertThrows(
                NullPointerException.class,
                () -> new CartItem(null, 1)
        );
    }

    @Test
    void couponRejectsNegativeAmountBlankCodeAndBackwardsDates() {
        var d = DATE;

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new Coupon(
                                "BAD",
                                new BigDecimal("-1"),
                                d, d
                        )
                ),

                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new Coupon(
                                " ",
                                BigDecimal.ONE,
                                d, d
                        )
                ),

                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new Coupon(
                                "BAD",
                                BigDecimal.ONE,
                                d, d.minusDays(1)
                        )
                )
        );
    }

    @Test
    void couponMayHaveSameStartAndEndDate() {

        var c = coupon(
                "1.00",
                DATE,
                DATE
        );

        assertEquals(DATE, c.startDate());
        assertEquals(DATE, c.endDate());
    }

    @Test
    void inventoryReservesAvailableStock() {

        var inventory = new InMemoryInventory(
                Map.of("SKU-1", 5)
        );

        inventory.reserve("SKU-1", 2);

        assertEquals(3, inventory.available("SKU-1"));
    }

    @Test
    void inventoryCanReserveExactAvailableStock() {

        var inventory = new InMemoryInventory(
                Map.of("SKU-1", 3)
        );

        inventory.reserve("SKU-1", 3);

        assertEquals(0, inventory.available("SKU-1"));
    }

    @Test
    void inventoryRejectsOverReservationAndInvalidQuantitiesWithoutMutation() {

        var inventory = new InMemoryInventory(
                Map.of("SKU-1", 5)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> inventory.reserve("SKU-1", 6)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> inventory.reserve("SKU-1", 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> inventory.reserve("SKU-1", -1)
        );

        assertEquals(5, inventory.available("SKU-1"));
        assertEquals(0, inventory.available("MISSING"));
    }
}