# Two significant defects

These reports select two specification violations for presentation. Severity is our assessment of impact, not a prescribed course severity scale. The implementation remains unchanged. Both use the fixed test date November 8, 2026.

## QC-01: Tax omits paid shipping from the taxable amount

- **Requirement:** Pricing rule 7 requires tax on discounted merchandise plus shipping; rule 8 requires HALF_UP rounding to two decimal places.
- **Inputs and reproduction:** Call `PricingService.quote` with one `SKU-1` item priced at $10.00, quantity 1, STANDARD tier, no coupon, province ON, and the test date. No discount applies; shipping is $8.99.
- **Expected:** Taxable amount $18.99; tax `18.99 × 0.13 = 2.4687`, rounded to **$2.47**. Total **$21.46**.
- **Actual:** Tax is calculated on merchandise alone: **$1.30**. Total **$20.29**, understating tax and total by **$1.17**.
- **Automated evidence:** `KnownDefectsTest.taxMustIncludeShipping` asserts the correct tax for ON, BC, and AB. [Saved failure report](coverage/test-results/known-defects/edu.testing.quickcart.KnownDefectsTest.txt) and [run log](coverage/known-defects-run.log).
- **Location:** [PricingService.java](../src/main/java/edu/testing/quickcart/service/PricingService.java), tax computation in `quote` uses `merchandise.multiply(taxRate)`.
- **Impact/severity:** **Medium**: orders with paid shipping have incorrect quoted tax and totals across the supported provinces. The error systematically affects monetary correctness rather than a cosmetic display.

Run from the repository root; this parameterized method runs all three provinces and is expected to fail:

```powershell
mvn test "-Dtest=KnownDefectsTest#taxMustIncludeShipping" "-Dquickcart.excludedGroups=none" "-Djacoco.destFile=target/jacoco-defect-tax.exec"
```

## QC-02: Declined payment still reduces inventory

- **Requirement:** Ordering rule 4 allows reservation only after successful authorization; rule 5 requires unchanged inventory when payment is declined.
- **Inputs and reproduction:** Start real `InMemoryInventory` with five `SKU-1` units. Use real pricing/order services and a fake `PaymentGateway` that always declines. Place customer `C-1`'s STANDARD ON order for two units priced at $200.00 each, no coupon, on the test date. The quote is $400.00 merchandise, $0.00 shipping, $52.00 tax, total $452.00.
- **Expected:** Return `PAYMENT_DECLINED` and retain **five** available units.
- **Actual:** Return `PAYMENT_DECLINED`, but only **three** units remain. Reservation occurs before authorization, and there is no rollback after the decline.
- **Automated evidence:** `KnownDefectsTest.declinedPaymentMustNotReduceInventory` asserts unchanged stock using real inventory. [Saved failure report](coverage/test-results/known-defects/edu.testing.quickcart.KnownDefectsTest.txt). The later [multi-product regression](mutation/test-results/regression/edu.testing.quickcart.KnownDefectsTest.txt) confirms that a decline can reduce two independent balances from 5/5 to 3/4. `authorizationMustHappenBeforeInventoryReservation` separately checks collaborator order.
- **Location:** [OrderService.java](../src/main/java/edu/testing/quickcart/service/OrderService.java), `placeOrder` reserves each line before calling `payment.authorize`.
- **Impact/severity:** **High**: failed purchases consume sellable stock. Repeated declines can make inventory unavailable to other customers without any accepted purchase.

Run from the repository root; the unchanged implementation is expected to fail the stock assertion:

```powershell
mvn test "-Dtest=KnownDefectsTest#declinedPaymentMustNotReduceInventory" "-Dquickcart.excludedGroups=none" "-Djacoco.destFile=target/jacoco-defect-decline.exec"
```

The wider regression suite also documents bulk-discount equality, free-shipping equality, coupon endpoint inclusivity, and exact-stock acceptance defects. These two reports are a selected pair, not a claim that only two defects exist.
