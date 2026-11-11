# P3 - Complete returned candidate output

The text below is the complete final response from the fresh Codex test-generation agent for P3 in [prompts.md](prompts.md), received November 11, 2026. It was saved before compilation or execution. The final sentence describes the generator's state at that point; later reviewer-run results are recorded separately in [evaluation.md](evaluation.md).

---

```java
package edu.testing.quickcart;

import edu.testing.quickcart.infra.InMemoryInventory;
import edu.testing.quickcart.model.CartItem;
import edu.testing.quickcart.model.CustomerTier;
import edu.testing.quickcart.model.Product;
import edu.testing.quickcart.service.OrderResult;
import edu.testing.quickcart.service.OrderService;
import edu.testing.quickcart.service.PaymentGateway;
import edu.testing.quickcart.service.PricingService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class AIExperimentOrderServiceTest {

    private static final LocalDate ORDER_DATE = LocalDate.of(2026, 11, 11);

    @Test
    void exactAvailableStockIsAcceptedAndReservedAfterAuthorization() {
        InMemoryInventory inventory = spy(
                new InMemoryInventory(Map.of("exact-stock", 2)));
        PaymentGateway payment = mock(PaymentGateway.class);
        OrderService service =
                new OrderService(new PricingService(), inventory, payment);
        List<CartItem> items = List.of(item("exact-stock", "60.00", 2));

        // $120 merchandise + $0 shipping + $15.60 ON tax.
        BigDecimal expectedTotal = new BigDecimal("135.60");
        when(payment.authorize("customer-exact", expectedTotal))
                .thenReturn(true);

        OrderResult result = service.placeOrder(
                "customer-exact", items, CustomerTier.STANDARD,
                null, "ON", ORDER_DATE);

        assertAll(
                () -> assertEquals(
                        OrderResult.Status.ACCEPTED, result.status()),
                () -> assertEquals(expectedTotal, result.quote().total()),
                () -> assertEquals(0, inventory.available("exact-stock"))
        );

        InOrder actions = inOrder(payment, inventory);
        actions.verify(payment).authorize("customer-exact", expectedTotal);
        actions.verify(inventory).reserve("exact-stock", 2);
    }

    @Test
    void declinedPaymentDoesNotReserveOrChangeInventory() {
        InMemoryInventory inventory = spy(
                new InMemoryInventory(Map.of("declined-item", 5)));
        PaymentGateway payment = mock(PaymentGateway.class);
        OrderService service =
                new OrderService(new PricingService(), inventory, payment);
        List<CartItem> items = List.of(item("declined-item", "60.00", 2));

        // $120 merchandise + $0 shipping + $15.60 ON tax.
        BigDecimal expectedTotal = new BigDecimal("135.60");
        when(payment.authorize("customer-declined", expectedTotal))
                .thenReturn(false);

        OrderResult result = service.placeOrder(
                "customer-declined", items, CustomerTier.STANDARD,
                null, "ON", ORDER_DATE);

        assertAll(
                () -> assertEquals(
                        OrderResult.Status.PAYMENT_DECLINED, result.status()),
                () -> assertEquals(expectedTotal, result.quote().total()),
                () -> verify(payment)
                        .authorize("customer-declined", expectedTotal),
                () -> assertEquals(5, inventory.available("declined-item")),
                () -> verify(inventory, never())
                        .reserve(anyString(), anyInt())
        );
    }

    @Test
    void laterOutOfStockItemPreventsPaymentAndAllReservations() {
        InMemoryInventory inventory = spy(new InMemoryInventory(
                Map.of("available-item", 5, "short-item", 2)));
        PaymentGateway payment = mock(PaymentGateway.class);
        OrderService service =
                new OrderService(new PricingService(), inventory, payment);
        List<CartItem> items = List.of(
                item("available-item", "60.00", 2),
                item("short-item", "40.00", 3)
        );

        OrderResult result = service.placeOrder(
                "customer-short", items, CustomerTier.STANDARD,
                null, "ON", ORDER_DATE);

        assertAll(
                () -> assertEquals(
                        OrderResult.Status.OUT_OF_STOCK, result.status()),
                () -> verifyNoInteractions(payment),
                () -> assertEquals(5, inventory.available("available-item")),
                () -> assertEquals(2, inventory.available("short-item")),
                () -> verify(inventory, never())
                        .reserve(anyString(), anyInt())
        );
    }

    private static CartItem item(
            String productId, String unitPrice, int quantity) {
        return new CartItem(
                new Product(productId, productId, new BigDecimal(unitPrice)),
                quantity);
    }
}
```

1. **Exact available stock:** Ordering requirements 1, 3, and 4 permit exact-stock purchases and require successful authorization before reservation. The oracle is acceptance, zero remaining stock, authorization for **$135.60**, and payment-before-reservation ordering. The total follows $120 merchandise, free shipping, and 13% ON tax. **Likely current result:** fails because the implementation rejects exact stock. After that defect is fixed, its ordering check can reveal premature reservation. **Limitations:** combines the stock boundary and success sequencing; it does not cover pricing boundaries or concurrency.

2. **Declined payment:** Ordering requirements 3–5 require authorization for the quoted total and unchanged inventory when declined. The oracle is `PAYMENT_DECLINED`, authorization for **$135.60**, all five units remaining, and no reservation call. **Likely current result:** fails because two units are reserved before authorization, leaving three. **Limitations:** covers an ordinary decline, excluding gateway exceptions and concurrent purchases.

3. **Later out-of-stock item:** Ordering requirement 2 prohibits payment and inventory changes if any item lacks stock. Placing the insufficient item second checks that an earlier valid item is not reserved prematurely. The oracle is `OUT_OF_STOCK`, unchanged quantities, no reservations, and no payment interaction. **Likely current result:** passes because all availability checks precede reservation. **Limitations:** covers two distinct products, excluding repeated product IDs and concurrent inventory changes.

These are candidates for human review; they were neither compiled nor run.
