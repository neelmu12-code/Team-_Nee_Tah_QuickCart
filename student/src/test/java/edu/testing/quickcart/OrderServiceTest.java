package edu.testing.quickcart;

import edu.testing.quickcart.model.*;
import edu.testing.quickcart.service.*;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static edu.testing.quickcart.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private final InventoryService inventory =
            mock(InventoryService.class);

    private final PaymentGateway payment =
            mock(PaymentGateway.class);

    private final OrderService service =
            new OrderService(
                    new PricingService(),
                    inventory,
                    payment
            );

    @Test
    void outOfStockSkipsPaymentAndDoesNotReserve() {

        when(inventory.available("SKU-1"))
                .thenReturn(1);

        var result = service.placeOrder(
                "C-1",
                List.of(item("200.00", 2)),
                CustomerTier.STANDARD,
                null,
                "ON",
                DATE
        );

        assertEquals(
                OrderResult.Status.OUT_OF_STOCK,
                result.status()
        );

        verify(inventory, never())
                .reserve(anyString(), anyInt());

        verifyNoInteractions(payment);
    }

    @Test
    void missingInventoryIsOutOfStock() {

        when(inventory.available("SKU-1"))
                .thenReturn(0);

        var result = service.placeOrder(
                "C-1",
                List.of(item("200.00", 1)),
                CustomerTier.STANDARD,
                null,
                "ON",
                DATE
        );

        assertEquals(
                OrderResult.Status.OUT_OF_STOCK,
                result.status()
        );

        verifyNoInteractions(payment);
    }

    @Test
    void availableInventoryAttemptsAuthorizationForTheQuotedTotal() {

        when(inventory.available("SKU-1"))
                .thenReturn(5);

        when(payment.authorize(
                anyString(),
                any(BigDecimal.class)
        )).thenReturn(true);

        var result = service.placeOrder(
                "C-99",
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

        var charged = ArgumentCaptor.forClass(
                BigDecimal.class
        );

        verify(payment).authorize(
                eq("C-99"),
                charged.capture()
        );

        assertEquals(
                0,
                result.quote().total()
                        .compareTo(charged.getValue())
        );

        verify(inventory).reserve("SKU-1", 2);
    }

    @Test
    void secondUnavailableItemPreventsAnyPaymentOrReservation() {

        var second = new Product(
                "SKU-2",
                "Second",
                new BigDecimal("2.00")
        );

        when(inventory.available("SKU-1"))
                .thenReturn(5);

        when(inventory.available("SKU-2"))
                .thenReturn(0);

        var result = service.placeOrder(
                "C-1",
                List.of(
                        item("10.00", 1),
                        new CartItem(second, 1)
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

        verifyNoInteractions(payment);

        verify(inventory, never())
                .reserve(anyString(), anyInt());
    }

    @Test
    void constructorRequiresAllDependencies() {

        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new OrderService(
                                null,
                                inventory,
                                payment
                        )
                ),

                () -> assertThrows(
                        NullPointerException.class,
                        () -> new OrderService(
                                new PricingService(),
                                null,
                                payment
                        )
                ),

                () -> assertThrows(
                        NullPointerException.class,
                        () -> new OrderService(
                                new PricingService(),
                                inventory,
                                null
                        )
                )
        );
    }
}