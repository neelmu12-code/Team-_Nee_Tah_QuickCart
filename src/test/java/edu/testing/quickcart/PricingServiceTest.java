package edu.testing.quickcart;

import edu.testing.quickcart.model.*;
import edu.testing.quickcart.service.PricingService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static edu.testing.quickcart.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

class PricingServiceTest {

    private final PricingService pricing = new PricingService();

    private OrderQuote quote(
            String price,
            int qty,
            CustomerTier tier,
            Coupon coupon,
            String province,
            LocalDate date) {

        return pricing.quote(
                List.of(item(price, qty)),
                tier,
                coupon,
                province,
                date
        );
    }

    @ParameterizedTest(name = "quantity {0} -> merchandise {1}")
    @CsvSource({
            "1, 10.00",
            "9, 90.00",
            "11, 104.50",
            "12, 114.00"
    })
    void quantityPartitionsOutsideKnownBulkBoundary(
            int quantity,
            String expected) {

        money(
                expected,
                quote(
                        "10.00",
                        quantity,
                        CustomerTier.STANDARD,
                        null,
                        "ON",
                        DATE
                ).merchandise()
        );
    }

    @Test
    void calculatesMultipleLineSubtotalsAndBulkDiscounts() {

        var second = new Product(
                "SKU-2",
                "Different",
                new BigDecimal("5.00")
        );

        var items = List.of(
                item("10.00", 2),
                new CartItem(second, 12)
        );

        var result = pricing.quote(
                items,
                CustomerTier.STANDARD,
                null,
                "ON",
                DATE
        );

        money("77.00", result.merchandise());
    }

    @Test
    void goldDiscountIsAppliedAfterBulkDiscount() {

        var result = quote(
                "20.00",
                11,
                CustomerTier.GOLD,
                null,
                "ON",
                DATE
        );

        money("188.10", result.merchandise());
    }

    @Test
    void standardTierDoesNotApplyGoldDiscount() {

        money(
                "40.00",
                quote(
                        "40.00",
                        1,
                        CustomerTier.STANDARD,
                        null,
                        "ON",
                        DATE
                ).merchandise()
        );
    }

    @Test
    void couponAtMinimumDiscountedMerchandiseThresholdIsValid() {

        var c = coupon(
                "5.00",
                DATE.minusDays(1),
                DATE.plusDays(1)
        );

        money(
                "45.00",
                quote(
                        "50.00",
                        1,
                        CustomerTier.STANDARD,
                        c,
                        "ON",
                        DATE
                ).merchandise()
        );
    }

    @Test
    void couponJustBelowMinimumIsNotApplied() {

        var c = coupon(
                "5.00",
                DATE.minusDays(1),
                DATE.plusDays(1)
        );

        money(
                "49.99",
                quote(
                        "49.99",
                        1,
                        CustomerTier.STANDARD,
                        c,
                        "ON",
                        DATE
                ).merchandise()
        );
    }

    @Test
    void couponMinimumIsBasedOnAlreadyDiscountedMerchandise() {

        var c = coupon(
                "10.00",
                DATE.minusDays(2),
                DATE.plusDays(2)
        );

        money(
                "49.50",
                quote(
                        "55.00",
                        1,
                        CustomerTier.GOLD,
                        c,
                        "ON",
                        DATE
                ).merchandise()
        );
    }

    @Test
    void couponOnlyAppliesWithinValidityWindow() {

        var expired = coupon(
                "10.00",
                DATE.minusDays(10),
                DATE.minusDays(1)
        );

        var upcoming = coupon(
                "10.00",
                DATE.plusDays(1),
                DATE.plusDays(10)
        );

        money(
                "60.00",
                quote(
                        "60.00",
                        1,
                        CustomerTier.STANDARD,
                        expired,
                        "ON",
                        DATE
                ).merchandise()
        );

        money(
                "60.00",
                quote(
                        "60.00",
                        1,
                        CustomerTier.STANDARD,
                        upcoming,
                        "ON",
                        DATE
                ).merchandise()
        );
    }

    @Test
    void oversizedCouponNeverMakesMerchandiseNegative() {

        var c = coupon(
                "150.00",
                DATE.minusDays(1),
                DATE.plusDays(1)
        );

        money(
                "0.00",
                quote(
                        "60.00",
                        1,
                        CustomerTier.STANDARD,
                        c,
                        "ON",
                        DATE
                ).merchandise()
        );
    }

    @Test
    void couponEqualToMerchandiseLeavesZeroBalanceAndPaidShipping() {
        var c = coupon("60.00", DATE.minusDays(1), DATE.plusDays(1));
        var q = quote("60.00", 1, CustomerTier.STANDARD, c, "ON", DATE);

        money("0.00", q.merchandise());
        money("8.99", q.shipping());
    }

    @Test
    void belowFreeShippingThresholdCostsEightNinetyNine() {

        money(
                "8.99",
                quote(
                        "99.99",
                        1,
                        CustomerTier.STANDARD,
                        null,
                        "ON",
                        DATE
                ).shipping()
        );
    }

    @Test
    void aboveFreeShippingThresholdIsFree() {

        money(
                "0.00",
                quote(
                        "100.01",
                        1,
                        CustomerTier.STANDARD,
                        null,
                        "ON",
                        DATE
                ).shipping()
        );
    }

    @ParameterizedTest
    @CsvSource({
            "ON, 26.00",
            "BC, 24.00",
            "AB, 10.00"
    })
    void usesProvincialTaxRatesOnOrdersWithoutShipping(
            String province,
            String expectedTax) {

        var q = quote(
                "200.00",
                1,
                CustomerTier.STANDARD,
                null,
                province,
                DATE
        );

        money(expectedTax, q.tax());
    }

    @Test
    void taxUsesHalfUpRoundingOnFreeShippingOrder() {

        var q = quote(
                "100.05",
                1,
                CustomerTier.STANDARD,
                null,
                "ON",
                DATE
        );

        money("13.01", q.tax());
        money("113.06", q.total());
    }

    @Test
    void exactHalfCentTaxRoundsUpRatherThanToEven() {
        // $100.50 x 5% = $5.025: HALF_EVEN would incorrectly return $5.02.
        var q = quote("100.50", 1, CustomerTier.STANDARD, null, "AB", DATE);
        money("5.03", q.tax());
        money("105.53", q.total());
    }

    @Test
    void couponIsSubtractedAfterBulkAndGoldDiscounts() {
        var c = coupon("10.00", DATE.minusDays(1), DATE.plusDays(1));
        var q = quote("20.00", 11, CustomerTier.GOLD, c, "ON", DATE);

        // $220 x 0.95 x 0.90 - $10 = $178.10; shipping is free.
        money("178.10", q.merchandise());
        money("0.00", q.shipping());
        money("23.15", q.tax());
        money("201.25", q.total());
    }

    @Test
    void couponCanRemoveFreeShippingEligibility() {
        var c = coupon("30.00", DATE.minusDays(1), DATE.plusDays(1));
        var q = quote("120.00", 1, CustomerTier.STANDARD, c, "ON", DATE);
        money("90.00", q.merchandise());
        money("8.99", q.shipping());
        // Tax on paid shipping is checked against the spec in KnownDefectsTest.
    }

    @Test
    void orderQuoteTotalIsMerchandisePlusShippingPlusTax() {

        var q = quote(
                "18.25",
                1,
                CustomerTier.STANDARD,
                null,
                "AB",
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

    @Test
    void supportsCaseInsensitiveProvinceCodes() {

        var q = quote(
                "200.00",
                1,
                CustomerTier.STANDARD,
                null,
                "on",
                DATE
        );

        money("26.00", q.tax());
    }

    @Test
    void rejectsUnknownProvince() {

        assertThrows(
                IllegalArgumentException.class,
                () -> quote(
                        "20.00",
                        1,
                        CustomerTier.STANDARD,
                        null,
                        "QC",
                        DATE
                )
        );
    }

    @Test
    void rejectsEmptyCart() {

        assertThrows(
                IllegalArgumentException.class,
                () -> pricing.quote(
                        List.of(),
                        CustomerTier.STANDARD,
                        null,
                        "ON",
                        DATE
                )
        );
    }

    @Test
    void rejectsNullRequiredInputs() {

        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> pricing.quote(
                                null,
                                CustomerTier.STANDARD,
                                null,
                                "ON",
                                DATE
                        )
                ),

                () -> assertThrows(
                        NullPointerException.class,
                        () -> pricing.quote(
                                List.of(item("1.00", 1)),
                                null,
                                null,
                                "ON",
                                DATE
                        )
                ),

                () -> assertThrows(
                        NullPointerException.class,
                        () -> pricing.quote(
                                List.of(item("1.00", 1)),
                                CustomerTier.STANDARD,
                                null,
                                null,
                                DATE
                        )
                ),

                () -> assertThrows(
                        NullPointerException.class,
                        () -> pricing.quote(
                                List.of(item("1.00", 1)),
                                CustomerTier.STANDARD,
                                null,
                                "ON",
                                null
                        )
                )
        );
    }
}
