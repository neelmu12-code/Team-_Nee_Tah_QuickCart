package edu.testing.quickcart;

import edu.testing.quickcart.model.*;
import edu.testing.quickcart.service.PricingService;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

import java.math.BigDecimal;
import java.util.List;

import static edu.testing.quickcart.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

class PricingPropertyTest {

    private final PricingService pricing = new PricingService();

    @Property(tries = 250, seed = "431301")
    void validCouponNeverProducesNegativeMerchandise(
            @ForAll @IntRange(min = 0, max = 30000)
            int unitPriceCents,

            @ForAll @IntRange(min = 1, max = 30)
            int quantity,

            @ForAll @IntRange(min = 0, max = 100000)
            int couponCents,

            @ForAll boolean gold) {

        BigDecimal price = BigDecimal.valueOf(
                unitPriceCents, 2
        );

        BigDecimal discount = BigDecimal.valueOf(
                couponCents, 2
        );

        Coupon coupon = new Coupon(
                "PROP",
                discount,
                DATE.minusDays(1),
                DATE.plusDays(1)
        );

        Product product = new Product(
                "P-PROP",
                "Property Product",
                price
        );

        OrderQuote quote = pricing.quote(
                List.of(new CartItem(product, quantity)),
                gold
                        ? CustomerTier.GOLD
                        : CustomerTier.STANDARD,
                coupon,
                "ON",
                DATE
        );

        assertTrue(
                quote.merchandise().signum() >= 0,
                () -> "Negative merchandise for price "
                        + price
                        + ", quantity " + quantity
                        + ", coupon " + discount
                        + ", gold=" + gold
        );
    }

    @Property(tries = 150, seed = "431302")
    void computedTotalIsAlwaysSumOfDisplayedComponents(
            @ForAll @IntRange(min = 0, max = 30000)
            int unitPriceCents,

            @ForAll @IntRange(min = 1, max = 30)
            int quantity) {

        Product product = new Product(
                "P-PROP",
                "Property Product",
                BigDecimal.valueOf(unitPriceCents, 2)
        );

        OrderQuote q = pricing.quote(
                List.of(new CartItem(product, quantity)),
                CustomerTier.STANDARD,
                null,
                "BC",
                DATE
        );

        assertEquals(
                0,
                q.merchandise()
                        .add(q.shipping())
                        .add(q.tax())
                        .compareTo(q.total())
        );
    }

    @Property(tries = 200, seed = "431303")
    void goldNeverIncreasesMerchandise(
            @ForAll @IntRange(min = 0, max = 30000) int unitPriceCents,
            @ForAll @IntRange(min = 1, max = 30) int quantity) {
        var cart = List.of(item(BigDecimal.valueOf(unitPriceCents, 2).toPlainString(), quantity));
        var standard = pricing.quote(cart, CustomerTier.STANDARD, null, "ON", DATE);
        var gold = pricing.quote(cart, CustomerTier.GOLD, null, "ON", DATE);

        assertTrue(gold.merchandise().compareTo(standard.merchandise()) <= 0,
                "GOLD's additional discount must never increase merchandise");
    }

    @Property(tries = 200, seed = "431304")
    void largerActiveCouponNeverIncreasesMerchandise(
            @ForAll @IntRange(min = 0, max = 30000) int unitPriceCents,
            @ForAll @IntRange(min = 1, max = 30) int quantity,
            @ForAll @IntRange(min = 0, max = 100000) int couponCents,
            @ForAll @IntRange(min = 0, max = 100000) int extraCents,
            @ForAll CustomerTier tier) {
        var cart = List.of(item(BigDecimal.valueOf(unitPriceCents, 2).toPlainString(), quantity));
        var small = coupon(BigDecimal.valueOf(couponCents, 2).toPlainString(),
                DATE.minusDays(1), DATE.plusDays(1));
        var large = coupon(BigDecimal.valueOf((long) couponCents + extraCents, 2).toPlainString(),
                DATE.minusDays(1), DATE.plusDays(1));

        var before = pricing.quote(cart, tier, small, "BC", DATE);
        var after = pricing.quote(cart, tier, large, "BC", DATE);
        assertTrue(after.merchandise().compareTo(before.merchandise()) <= 0,
                "Increasing an active coupon must never increase merchandise");
    }
}
