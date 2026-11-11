// Derived from evidence/ai/candidate-output.md. Concrete spies were replaced
// with interface mocks delegating to real inventory for Java 25 compatibility.
// Expected values, assertions, and behavior under test remain unchanged.
package edu.testing.quickcart;

import edu.testing.quickcart.infra.InMemoryInventory;
import edu.testing.quickcart.model.CartItem;
import edu.testing.quickcart.model.CustomerTier;
import edu.testing.quickcart.model.Product;
import edu.testing.quickcart.service.InventoryService;
import edu.testing.quickcart.service.OrderResult;
import edu.testing.quickcart.service.OrderService;
import edu.testing.quickcart.service.PaymentGateway;
import edu.testing.quickcart.service.PricingService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@Tag("known-defect")
@Tag("recorded-ai-experiment")
public class AIExperimentKnownDefectsTest {

    private static final LocalDate ORDER_DATE = LocalDate.of(2026, 11, 11);

    @Test
    void exactAvailableStockIsAcceptedAndReservedAfterAuthorization() {
        InventoryService inventory = mock(InventoryService.class, delegatesTo(
                new InMemoryInventory(Map.of("exact-stock", 2))));
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
        InventoryService inventory = mock(InventoryService.class, delegatesTo(
                new InMemoryInventory(Map.of("declined-item", 5))));
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

    private static CartItem item(
            String productId, String unitPrice, int quantity) {
        return new CartItem(
                new Product(productId, productId, new BigDecimal(unitPrice)),
                quantity);
    }
}
