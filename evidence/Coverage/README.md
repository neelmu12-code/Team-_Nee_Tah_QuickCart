# 5. Coverage Analysis

## Evidence and scope

Measured on November 10, 2026 against commit `3d24778a326480814cd5ad995933c4b2825f7d51`, using the **student module**, JaCoCo 0.8.14, Maven 3.9.16, and Eclipse Adoptium Java 21.0.4 (compilation target Java 17). Production code and tests were not changed for this analysis.

Open [the saved HTML report](jacoco/index.html) in a browser to inspect packages, classes, and highlighted source lines. [CSV](jacoco/jacoco.csv) and [XML](jacoco/jacoco.xml) contain the raw counters. The report is a snapshot, so it remains available after cleaning `student/target`.

The measured default suite includes `AITest`, `AIOrderServiceTest`, the pricing and ordering unit tests, integration tests, model/inventory tests, the smoke test, and jqwik properties. The module's existing Surefire configuration excludes the `known-defect` tag. Therefore, **these coverage figures describe the passing suite, not all specification tests**.

## Results

The default run completed successfully: **50 tests, 0 failures, 0 errors, 0 skipped**. This count includes four property methods; their 800 generated checks (250 + 150 + 200 + 200) are not 800 separately reported Surefire tests. Per-class test summaries are saved under [test-results/default](test-results/default/).

| Metric | Covered / total | Coverage |
| --- | ---: | ---: |
| Instructions | 522 / 522 | 100% |
| Branches | 52 / 52 | 100% |
| Lines | 77 / 77 | 100% |
| Methods | 17 / 17 | 100% |
| Classes | 10 / 10 | 100% |

These are JaCoCo's measured executable elements, not counts of every source declaration. The two service interfaces do not appear as executable classes in this report. A dash below means the class has no measured branches.

| Class | Lines covered / total | Branches covered / total |
| --- | ---: | ---: |
| PricingService | 36 / 36 | 26 / 26 |
| OrderService | 15 / 15 | 8 / 8 |
| InMemoryInventory | 7 / 7 | 4 / 4 |
| Product | 5 / 5 | 6 / 6 |
| Coupon | 6 / 6 | 6 / 6 |
| CartItem | 4 / 4 | 2 / 2 |
| CustomerTier | 1 / 1 | — |
| OrderQuote | 1 / 1 | — |
| OrderResult | 1 / 1 | — |
| OrderResult.Status | 1 / 1 | — |

## What is well covered

`PricingServiceTest` exercises bulk-discount partitions, GOLD discount ordering, the coupon minimum, active/inactive coupons, the nonnegative merchandise clamp, shipping above and below its threshold, provincial tax rates, rounding, and invalid inputs. `PricingPropertyTest` broadens the inputs for nonnegative merchandise, component totals, GOLD monotonicity, and coupon monotonicity. These properties complement explicit expected-value assertions; they do not independently establish that every tax or discount rule is correct.

`OrderServiceTest` and `OrderIntegrationTest` exercise acceptance and insufficient stock, including an unavailable second line and checks that payment/reservation do not occur for an out-of-stock order. `AIOrderServiceTest` exercises the payment-declined return and a successful multi-product order with exact payment amounts. `ModelAndInventoryTest` exercises constructor constraints, successful reservations, exact-stock reservation directly on the inventory, and invalid reservations without mutation.

## What full coverage does not establish

There are **no missed lines or branches in this snapshot**. However, executing both sides of a condition does not prove that its boundary is correct, and executing statements does not prove that they occur in the required order. A separate run of `KnownDefectsTest` produced **11 failures, 0 errors, 0 skipped**. See [the run log](known-defects-run.log) and [the failure report](test-results/known-defects/edu.testing.quickcart.KnownDefectsTest.txt).

| Specification requirement | Observed failure | Why 100% coverage can coexist with it |
| --- | --- | --- |
| Bulk discount starts at quantity 10 | Ten $10 items cost $100 merchandise instead of $95. | Values below and above 10 cover both outcomes of `> 10` without validating equality. |
| Shipping is free at $100 | Shipping is $8.99 instead of $0. | Both shipping outcomes execute even with an incorrect `>` threshold. |
| Coupon dates include both endpoints | Start-date, end-date, and single-day coupons leave $60 merchandise instead of $55. | Active/inactive paths do not establish inclusive date semantics. |
| Tax includes shipping | On a $10 order, ON/BC/AB tax is $1.30/$1.20/$0.50 rather than $2.47/$2.28/$0.95. | All tax-rate choices execute; checking rates on free-shipping orders cannot reveal the missing shipping tax base. |
| Exact available stock is sufficient | Requesting all three available units returns OUT_OF_STOCK. | Both stock-check outcomes execute with the wrong equality comparison. Direct inventory reservation coverage does not validate the order service's check. |
| Declined payment preserves inventory | Stock falls from 5 to 3 despite the decline. | The declined-status assertion passes without checking inventory state. |
| Authorization precedes reservation | Mockito's interaction-order verification fails. | Statement/branch coverage does not measure collaborator call order. |

The 11 failing invocations represent seven requirement areas, not 11 independent root causes: tax is parameterized across three provinces, and coupon validity has three date cases. These requirements already have regression tests, but those tests are excluded from the default passing run. They are detected defects, not reasons to weaken the assertions or alter production code for this evidence task.

The smoke test and `AITest.ordinaryCartReturnsPositiveTotal` also illustrate limited assertions: a positive total can still contain incorrect shipping, tax, or discount amounts. Similarly, `AIOrderServiceTest` checks the declined result and quoted amount but explicitly leaves stock preservation to the separate known-defect test. High structural coverage must be interpreted alongside assertion strength and the excluded tests.

## Would additional coverage add value?

More tests cannot raise the measured line/branch percentages above this snapshot. The highest-value next step is to resolve the known specification defects in an authorized implementation task and then include their regression tests in the normal passing suite. Those tests improve confidence even if the coverage counters stay unchanged.

Additional scenarios could improve behavioral confidence without changing the percentages:

- Test repeated cart lines for the same SKU. Current availability checks examine each line independently; a combined demand greater than stock may expose a reservation failure or partial mutation. This is a candidate to investigate, not a defect established by this run.
- Test a declined multi-product order with real inventory, asserting that every stock balance remains unchanged. This extends the existing single-product defect regression to a broader transaction.
- If dependency failures are in scope, first define expected behavior for a payment gateway exception or an inventory reservation exception, then test the agreed state guarantees. The current specification does not define these failure contracts.

Adding more ordinary happy-path inputs solely to maintain a percentage offers less value than checking boundaries, exact monetary results, and inventory/payment side effects. Mutation testing would provide separate evidence about assertion sensitivity; no mutation score is claimed here.

## Reproduce

The November 10 snapshot above was measured in the original `student/` module. On November 11 the executable POM and unchanged production sources moved to the repository root to match the required submission layout. The commands below use that current layout and may include later test additions; they do not recreate the historical test selection automatically.

Run from the repository root in PowerShell.

```powershell
mvn clean test
```

This generates `target/site/jacoco/index.html`, `jacoco.csv`, and `jacoco.xml`. The checked-in `jacoco/` folder remains the saved output of the original 50-test run. Save new output before running other configurations. The later controlled AI experiment has its own snapshots under [../ai/measurements/](../ai/measurements/).

Run the excluded specification regressions separately:

```powershell
mvn -B -ntp test "-Dgroups=known-defect" "-Dquickcart.excludedGroups=none" "-Djacoco.destFile=target/jacoco-known-defects.exec"
```

This command is expected to exit with a test failure for the measured revision. It uses a separate execution-data file so it does not append failing-test coverage to the passing suite's data. Maven stops at the test failures, so this second run does not generate a new JaCoCo report. The saved HTML/CSV/XML all refer to the first, successful run.
