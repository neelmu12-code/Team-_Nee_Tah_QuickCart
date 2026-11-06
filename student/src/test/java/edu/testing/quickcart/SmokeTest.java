package edu.testing.quickcart;
import edu.testing.quickcart.model.*;
import edu.testing.quickcart.service.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class SmokeTest {
    @Test void quoteReturnsPositiveTotal() {
        Product p = new Product("P1", "Cable", new BigDecimal("10.00"));
        OrderQuote q = new PricingService().quote(List.of(new CartItem(p,1)), CustomerTier.STANDARD,
                null, "ON", LocalDate.of(2026,9,1));
        assertTrue(q.total().signum() > 0);
    }
}
