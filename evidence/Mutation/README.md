# 6. Mutation Testing

## Scope and evidence

PIT was run on November 10, 2026 against the student module, starting at commit `4a8722eaae7ea52b7bd7fcc5371f9ebd81c311ba`. Tools: PIT 1.20.7, pitest-junit5-plugin 1.2.3, Maven 3.9.16, Eclipse Adoptium Java 21.0.4, and compilation target Java 17. Both runs used the same production code, default mutators, target patterns `edu.testing.quickcart.*`, and two worker threads. No production code or mutation configuration was changed.

| Evidence | Before test additions | After test additions |
| --- | --- | --- |
| Interactive HTML report | [before/index.html](before/index.html) | [after/index.html](after/index.html) |
| Machine-readable results | [before/mutations.xml](before/mutations.xml) | [after/mutations.xml](after/mutations.xml) |
| Full build/test/PIT log | [before-run.log](before-run.log) | [after-run.log](after-run.log) |

Open the HTML files in a browser to inspect source lines and individual mutants. [survivors.csv](survivors.csv) lists the surviving mutations from both runs, and [test-changes.patch](test-changes.patch) records the test additions.

The normal suite includes the AI tests, unit tests, integration tests, and jqwik properties. `KnownDefectsTest` is explicitly excluded from PIT, and its `known-defect` tag is excluded from the default Surefire run. These excluded tests assert specification behavior that the original implementation violates; mixing them into PIT's baseline would introduce already-failing tests. This limits the meaning of the score and is central to interpreting the survivors below.

## Results and change

| Measure | Before | After |
| --- | ---: | ---: |
| Default Surefire tests | 50 passed | 51 passed |
| Generated mutants | 31 | 31 |
| Killed | 27 | 27 |
| Survived | 4 | 4 |
| No coverage | 0 | 0 |
| Timeouts / execution errors | 0 | 0 |
| Mutation score: killed / generated | 87.1% | 87.1% |
| PIT line coverage, mutated classes only | 58 / 58 (100%) | 58 / 58 (100%) |

PIT rounds the mutation score and test strength to 87%. Its 58 measured lines concern the classes receiving mutations and should not be substituted for the separate JaCoCo report's 77 lines. PIT reports 148 mutant-test executions before and 151 after; those are not counts of unique suite tests.

| Mutated class | Killed / generated, both runs | Survivors |
| --- | ---: | ---: |
| InMemoryInventory | 7 / 7 | 0 |
| OrderService | 6 / 7 | 1 |
| PricingService | 14 / 17 | 3 |

Only these three classes received mutants with this tool configuration. A lack of generated mutants for a model or interface is not evidence that every possible fault there is detectable. All four survivors are conditional-boundary mutations; the other generated mutation types were killed.

## Survivor investigation

The line numbers below refer to the production source embedded in both reports. Classification comes from inspecting the original comparison, the boundary mutation, the specification, and the existing regression tests. The three corrective mutations are behaviorally different from the original; they must not be labeled equivalent merely because the passing suite lets them survive.

| Location | Original → mutant | Assessment and decision |
| --- | --- | --- |
| PricingService.java:24 | quantity `> 10` → `>= 10` | **Corrective mutant.** For ten $10 items, the original returns $100 merchandise and the mutant returns the specified $95. Normal tests exercise quantities 9 and 11 but omit equality. `KnownDefectsTest.bulkDiscountMustStartAtQuantityTen` already asserts the correct boundary and fails on the original. Do not add an assertion requiring $100 just to kill this mutant. |
| PricingService.java:40 | merchandise `> 100` → `>= 100` for free shipping | **Corrective mutant.** At exactly $100, the mutant supplies the specified free shipping; the original charges $8.99. The excluded `shippingMustBeFreeAtExactlyOneHundred` test already records this defect. Values on either side provide full branch coverage without testing equality. |
| OrderService.java:23 | available stock `<=` quantity → `<` quantity for rejection | **Corrective mutant.** With three units available and three requested, the mutant accepts the order, matching the specification, while the original rejects it. The excluded `exactStockIsEnoughForAnOrder` test captures the requirement. The direct inventory exact-reservation test does not exercise this order-service comparison. |
| PricingService.java:37 | merchandise sign `< 0` → `<= 0` for clamping | **Equivalent for the returned quote.** Negative balances are reset to zero in both versions; positive balances are unchanged. At zero, the original keeps zero and the mutant assigns `BigDecimal.ZERO`. Both subsequently normalize merchandise to scale 2 and produce identical shipping, tax, and total values. The local pre-normalization zero object's scale/identity is not exposed. There is no useful observable distinction to assert. |

These survivors expose a limitation of the default passing suite: it does not enforce three important equality requirements. The wider repository already detects those faults through excluded specification regressions. Mutation testing here measures sensitivity to changes in a defective baseline, so a survivor can improve correctness rather than represent an additional fault.

## Test improvements and validation

Two tests were added without changing production behavior:

1. `PricingServiceTest.couponEqualToMerchandiseLeavesZeroBalanceAndPaidShipping`: a valid $60 coupon on $60 merchandise must leave $0.00 merchandise and $8.99 shipping. Existing tests checked an oversized coupon; this adds exact exhaustion and checks that a zero merchandise balance does not receive free shipping. It passes on the original implementation and on the equivalent clamp mutant. It intentionally makes no tax assertion because tax on shipping has a separate known defect.
2. `KnownDefectsTest.declinedMultiProductPaymentMustPreserveEveryStockBalance`: a real inventory starts with five units each of two products, then receives a declined order for two and one units. It verifies the declined status, the exact $621.50 authorization amount, and both unchanged stock balances. It extends the single-product defect regression to a transaction affecting multiple balances. It remains under the existing `known-defect` tag and PIT exclusion.

The updated normal suite passed all **51 tests** with no failures or errors, and the PIT run succeeded. The new multi-product regression was also run separately: **one test failed**, with both stock assertions failing because balances became **3 and 4 instead of 5 and 5**. This is a confirmed specification violation, not a tool error. See [new-regression-run.log](new-regression-run.log) and [its Surefire report](test-results/regression/edu.testing.quickcart.KnownDefectsTest.txt). Passing-run summaries are under [test-results/after](test-results/after/).

The score remained **27/31 (87.1%)**, and the same four mutants survived. This outcome is appropriate: the exact-zero mutation is equivalent, the three corrective mutants should not be killed by tests that enforce incorrect behavior, and the new failing regression is excluded from both PIT runs. The tests improve behavioral evidence without manufacturing a score increase.

## Interpretation and next steps

The killed mutants provide evidence that the passing suite detects the generated conditional inversions, removed reservation-related calls, return-value changes, and inventory arithmetic changes. They do not establish that every requirement is correct. In particular, full line coverage and an 87% mutation score coexist with known tax-base, coupon-date, stock-boundary, and transaction-order defects documented in the coverage evidence.

The useful next implementation step would be to fix the specification defects under an authorized change and bring their regressions into the passing suite, then rerun PIT against that corrected baseline. This task leaves production code unchanged. No score adjusted by removing survivors is claimed, and no custom mutation operators or broader mutation campaign were run. Default operators also do not demonstrate sensitivity to every BigDecimal formula error or collaborator-order fault.

## Reproduce

These are current-layout commands. The saved November 10 runs above used the original `student/` module. On November 11 the executable POM and unchanged production sources moved to the root; later AI experiment additions have their own [separate measurements](../ai/measurements/). Running the current suite does not recreate the historical before/after selections automatically.

From the repository root in PowerShell:

```powershell
mvn -B -ntp test org.pitest:pitest-maven:mutationCoverage
```

The generated report is `target/pit-reports/index.html`, with XML beside it. Copy the whole report directory before another run because the configuration uses non-timestamped reports. The saved `before/` report predates the two additions recorded in `test-changes.patch`; `after/` includes them. Both historical runs used the same command and configuration.

To reproduce the new, intentionally failing specification regression separately:

```powershell
mvn -B -ntp test "-Dtest=KnownDefectsTest#declinedMultiProductPaymentMustPreserveEveryStockBalance" "-Dquickcart.excludedGroups=none" "-Djacoco.destFile=target/jacoco-mutation-regression.exec"
```

That command is expected to exit with a test failure until the inventory/payment defect is fixed. The separate JaCoCo execution file keeps this diagnostic run apart from default coverage data. The earlier [coverage analysis](../coverage/README.md) remains a historical snapshot of its original 50-test run.
