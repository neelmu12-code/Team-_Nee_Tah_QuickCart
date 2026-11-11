# Testing strategy

This concise strategy records the approach demonstrated by the implemented suite. It was consolidated on November 11, 2026, after implementation; it is not evidence that a written plan existed before the tests were developed. The oracle is [PROJECT_SPECIFICATION.md](../PROJECT_SPECIFICATION.md), rather than the current implementation.

| Behaviour or risk | Technique and important inputs | Oracle and evidence |
| --- | --- | --- |
| Discount and shipping boundaries | Equivalence partitions and boundary values: bulk quantities 9/10/11; coupon minimum $49.99/$50; shipping merchandise $99.99/$100/$100.01; standard/GOLD tiers | Exact `BigDecimal` comparisons in `PricingServiceTest`; specification boundaries in `KnownDefectsTest` |
| Coupon eligibility and discount ordering | No coupon, active/inactive coupon, both date endpoints, a single-day coupon, exact/oversized discounts, bulk then GOLD then coupon | Expected merchandise values and nonnegative clamp; endpoint failures retained as defect evidence |
| Tax and monetary rounding | Parameterized ON/BC/AB cases; free/paid shipping; exact HALF_UP rounding cases | Independently calculated expected tax; paid-shipping tax regressions demonstrate the incorrect tax base |
| Invalid inputs and model constraints | Empty cart, nonpositive quantity, negative price, unsupported province, invalid constructor arguments/reservations | Expected exceptions and unchanged inventory where applicable in `PricingServiceTest` and `ModelAndInventoryTest` |
| Order/payment interactions | Mockito inventory/payment doubles; sufficient/insufficient stock, later unavailable line, exact available stock, decline, authorization/reservation order | Exact authorization argument, `never`/no-interaction checks, captured arguments, and `InOrder`; isolate external payment and expose side effects |
| Real components working together | Real `PricingService`, `OrderService`, and `InMemoryInventory`, with a fake payment gateway | Accepted order reduces real stock; unavailable second product preserves all balances and avoids payment in `OrderIntegrationTest` |
| Broad input variation | jqwik generates prices, quantities, coupon values, and tiers with reproducible seeds | Four properties below, alongside exact example-based oracles |
| Untested execution paths | JaCoCo HTML/CSV/XML | Inspect lines/branches and explain why full execution coverage does not prove correct boundaries, formulas, or interaction order; [coverage analysis](coverage/README.md) |
| Weak assertions and missed changes | PIT, survivor inspection, and recorded test additions | Assess each survivor against the specification; preserve equivalent/corrective survivors without inventing incorrect assertions; [mutation analysis](mutation/README.md) |
| AI-generated test quality | Retained prompt/output, code/oracle review, and controlled comparison | Review oracle correctness, redundancy, missed cases, actual test outcomes, and measured effect; [AI evaluation](ai/evaluation.md) |

## Properties in ordinary language

The four methods in [PricingPropertyTest.java](../src/test/java/edu/testing/quickcart/PricingPropertyTest.java) exercise 800 generated checks per successful run:

1. A valid coupon must never make discounted merchandise negative (250 checks).
2. The displayed total must equal displayed merchandise plus shipping plus tax (150 checks).
3. GOLD status must never increase merchandise compared with STANDARD status for the same cart (200 checks).
4. A larger active coupon must never increase merchandise for the same eligible cart and tier (200 checks).

Generated prices span $0 to $300, quantities 1 to 30, and coupon inputs include zero and discounts larger than merchandise. These relational properties explore many inputs, but do not independently establish the exact tax formula, date inclusivity, discount rate, or stock/payment ordering. Fixed seeds make the runs reproducible; four property methods are four Surefire tests, not 800 separately reported tests.

## Running and interpreting the suite

From the repository root:

```powershell
mvn clean test
mvn test "-Dgroups=known-defect" "-Dquickcart.excludedGroups=none" "-Djacoco.destFile=target/jacoco-known-defects.exec"
```

The normal suite excludes the `known-defect` tag. The second command deliberately runs correct specification assertions that the unchanged implementation violates and is expected to fail. PIT also excludes those already-failing regression tests so its unmutated baseline can pass. Always distinguish a passing normal suite from compliance with every requirement. Production code has not been repaired to obtain a passing result.
