package edu.testing.quickcart;

import edu.testing.quickcart.model.*;
import edu.testing.quickcart.service.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static edu.testing.quickcart.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** AI-assisted ordering tests; compare their effect with -Pwithout-ai. */
@Tag("ai-experiment")
class AIOrderServiceTest {
    @Test
    void declinedAuthorizationReturnsDeclinedStatusAndTheQuotedAmount() {
        InventoryService inventory = mock(InventoryService.class);
        PaymentGateway payment = mock(PaymentGateway.class);
        when(inventory.available("SKU-1")).thenReturn(5);
        // Mockito's default false return represents a declined authorization.
        var service = new OrderService(new PricingService(), inventory, payment);

        var result = service.placeOrder("C-DECLINED", List.of(item("200.00", 2)),
                CustomerTier.STANDARD, null, "ON", DATE);

        assertNotNull(result);
        assertEquals(OrderResult.Status.PAYMENT_DECLINED, result.status());
        money("400.00", result.quote().merchandise());
        money("0.00", result.quote().shipping());
        money("52.00", result.quote().tax());
        money("452.00", result.quote().total());
        verify(payment).authorize("C-DECLINED", new BigDecimal("452.00"));
        // The separate known-defect test checks that a decline preserves stock.
    }

    @Test
    void approvedMultiProductOrderReservesEachLineAndChargesTheExpectedTotal() {
        InventoryService inventory = mock(InventoryService.class);
        PaymentGateway payment = mock(PaymentGateway.class);
        when(inventory.available("SKU-1")).thenReturn(5);
        when(inventory.available("SKU-2")).thenReturn(5);
        when(payment.authorize("C-APPROVED", new BigDecimal("621.50"))).thenReturn(true);
        var second = new Product("SKU-2", "Second item", new BigDecimal("150.00"));
        var service = new OrderService(new PricingService(), inventory, payment);

        var result = service.placeOrder("C-APPROVED",
                List.of(item("200.00", 2), new CartItem(second, 1)),
                CustomerTier.STANDARD, null, "ON", DATE);

        assertEquals(OrderResult.Status.ACCEPTED, result.status());
        money("550.00", result.quote().merchandise());
        money("71.50", result.quote().tax());
        money("621.50", result.quote().total());
        verify(payment).authorize("C-APPROVED", new BigDecimal("621.50"));
        verify(inventory).reserve("SKU-1", 2);
        verify(inventory).reserve("SKU-2", 1);
    }
}
