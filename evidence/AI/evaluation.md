# AI-assisted testing: evidence and critical evaluation

Recorded November 11, 2026. Team: Neel Upadhyay and Taha Qureshi; team number pending. This evaluation and the accompanying review were prepared with Codex assistance. Both team members remain responsible for checking, understanding, and explaining the submission. No historical human review or unrecorded AI conversation is asserted.

## 1. Historical planning assistance supplied by the team

Tool visible in the screenshots: ChatGPT; exact model/version and original conversation date are not visible. The [eight unmodified original screenshots](screenshots/README.md) are primary artifacts. [prompts.md](prompts.md) records both visible prompts, and [relevant-output.md](relevant-output.md) transcribes relevant visible output.

The responses usefully identify the main testing techniques, evidence requirements, the importance of interpreting coverage/mutation scores, and the requirement to preserve the supplied production implementation. The proposed root-level Maven layout matches the assignment's layout, with optional planning files added. Its example test filenames are suggestions, not the final application's API or proof that runnable tests existed.

The output explicitly admits that templates were disabled, the property was unimplemented, and Maven had not been run. Thus the status checklist's "prepared" items indicate a proposed skeleton, not completed testing. The screenshots alone provide no test bodies, test oracles, measured coverage gain, mutant kills, or demonstrated defects. They cannot establish the provenance of the older `AITest` or `AIOrderServiceTest` bodies. We retain the screenshots accurately as planning assistance and evaluate executable test generation separately below.

## 2. Prospective controlled test-generation experiment

Tool: Codex, using a fresh agent given the exact P3 prompt in [prompts.md](prompts.md). The exact model/version was not exposed and is not guessed. The generator was instructed to read only the behavioural specification and production source, avoid existing tests/evidence/history, write no files, and return three self-contained ordering test candidates. Its entire returned final response is retained in [candidate-output.md](candidate-output.md).

The experiment inputs are the repository's [behavioural specification](../../PROJECT_SPECIFICATION.md) and [production sources](../../src/main/java/edu/testing/quickcart/). All 11 production files were checked against their pre-experiment hashes after the runs; their contents were unchanged. Moving the POM and source tree from `student/` to the required root layout occurred before both measurements.

### Control and treatment

- **Control:** the full pre-existing passing suite, with only the newly recorded experiment classes excluded via `-Pwithout-recorded-ai`. Older `AITest` and `AIOrderServiceTest` remain included. It is a pre-existing-suite comparator, not a claim that every control test was written without AI assistance.
- **Treatment:** that same suite plus the new passing later-out-of-stock candidate. The two generated specification-failing cases run separately as diagnostics; they are excluded from both default/PIT baselines because unmutated PIT must start with passing tests.
- **Held constant:** production contents, existing tests, JDK, Maven, configured dependencies, PIT default operators, two PIT worker threads, and target patterns. Both successful measurements used clean builds.
- **Outcome measures:** Surefire results, JaCoCo line/branch/instruction counters, generated/killed/surviving mutants, and whether generated diagnostic cases reveal a defect not already documented.

Both runs used Maven 3.10.0, Java 25.0.2, Java compilation release 17, JUnit 5.11.4, Mockito 5.15.2, jqwik 1.9.2, JaCoCo 0.8.14, PIT 1.20.7, and PIT JUnit 5 plugin 1.2.3. See [environment.txt](measurements/environment.txt) and the [exact commands and run history](measurements/commands.txt). The temporary dependency-cache path and offline option are environment details, not prerequisites for another normal checkout.

## 3. Review and actual compatibility failure

The raw candidate uses concrete `spy(InMemoryInventory)` objects. The first treatment run compiled but failed when Mockito/Byte Buddy tried to instrument JDK 25 `Object`: the installed Byte Buddy 1.15.11 rejects class-file version 69. This was **a tool compatibility error, not evidence of an incorrect QuickCart assertion**. The [complete initial failure log](measurements/treatment-initial-failure/run.log) and [original candidate code](candidate-output.md) preserve the evidence. PIT did not run for that failed treatment.

The reviewed adaptation replaces each concrete spy with an `InventoryService` interface mock using `AdditionalAnswers.delegatesTo(new InMemoryInventory(...))`. Calls still change/check real inventory state and can be verified as collaborator interactions, while avoiding concrete/JDK-class instrumentation. No production code, expected monetary values, or assertion was weakened. No dependencies were upgraded to change the outcome.

The generated class was also split to isolate intentional specification failures:

- [AIExperimentOrderServiceTest.java](../../src/test/java/edu/testing/quickcart/AIExperimentOrderServiceTest.java): the passing later-out-of-stock case, tagged `recorded-ai-experiment`.
- [AIExperimentKnownDefectsTest.java](../../src/test/java/edu/testing/quickcart/AIExperimentKnownDefectsTest.java): the two correct defect-revealing cases, tagged `known-defect` and `recorded-ai-experiment`, and excluded from PIT.

Only imports/class organization, tags, origin comments, and the test-double construction changed from the candidate output. The raw output and runnable test sources remain available for comparison.

## 4. Candidate-by-candidate assessment

| Candidate | Oracle and observed behaviour | Value and limitation |
| --- | --- | --- |
| `exactAvailableStockIsAcceptedAndReservedAfterAuthorization` | Two $60 units produce $120 merchandise, free shipping, $15.60 ON tax, and $135.60 total. Exact stock should be accepted and consumed only after approval. The current code rejects it and leaves two units. | Correct stock-boundary oracle. It reconfirms an already documented defect. It combines boundary acceptance and interaction order: the failing `assertAll` prevents its later `InOrder` assertion from running, so this run cannot independently establish the sequencing failure. The existing dedicated ordering regression remains important. |
| `declinedPaymentDoesNotReserveOrChangeInventory` | The same $135.60 quote is authorized and declined. Expected stock is five with no reservation; actual stock is three and a reservation occurred. | Correct exact total plus state/interaction checks. It reconfirms an existing transactional defect rather than discovering a new one. Real inventory delegation makes the stock assertion meaningful. |
| `laterOutOfStockItemPreventsPaymentAndAllReservations` | With stock 5 and 2, requested quantities 2 and 3 must return `OUT_OF_STOCK`, leave both balances unchanged, and avoid payment and all reservations. The reviewed candidate passes. | Meaningful negative integration/interaction case, but overlaps existing ordering and integration tests. Retained as a small reproducible experiment treatment; no novelty or adequacy increase is credited. |

The $135.60 monetary oracle was checked directly from the specification: `2 * $60 = $120`, no bulk/GOLD/coupon discount, free shipping above $100, and `120 * 0.13 = $15.60`. These inputs intentionally avoid the unrelated paid-shipping tax defect, allowing the ordering requirements to be assessed independently. `BigDecimal` equality is appropriate here because both the expected and returned amounts have scale 2.

## 5. Measured results

| Measure | Control: pre-existing suite | Reviewed treatment |
| --- | ---: | ---: |
| Surefire tests | 51 passed | 52 passed |
| Failures / errors / skipped | 0 / 0 / 0 | 0 / 0 / 0 |
| JaCoCo lines | 77/77 (100%) | 77/77 (100%) |
| JaCoCo branches | 52/52 (100%) | 52/52 (100%) |
| JaCoCo instructions | 522/522 (100%) | 522/522 (100%) |
| PIT generated / killed / survived | 31 / 27 / 4 | 31 / 27 / 4 |
| PIT mutation score | 87.1% | 87.1% |
| Newly killed survivors | - | 0 |

Primary evidence: [control log](measurements/control/run.log), [control JaCoCo XML](measurements/control/jacoco.xml), [control PIT XML](measurements/control/mutations.xml), [treatment log](measurements/treatment/run.log), [treatment JaCoCo XML](measurements/treatment/jacoco.xml), and [treatment PIT XML](measurements/treatment/mutations.xml). The full logs retain the test summaries; the XMLs retain raw coverage counters and individual mutation outcomes.

The new passing test increases the test count by one, with **zero coverage gain and zero additional mutant kills**. Direct comparison of the mutation XMLs confirms the same four survivor identities, and the added test is not named as the killing test of any mutant. [summary.json](measurements/summary.json) records the parsed metrics, survivor identities, and production-hash verification.

The separate diagnostic run executed **14 tests: 14 expected failures, zero errors, zero skipped**. Twelve invocations belong to the pre-existing `KnownDefectsTest` and two to `AIExperimentKnownDefectsTest`. The generated cases expose already known specification violations; they do not represent two newly discovered root causes. The separately saved results are under [diagnostic/](measurements/diagnostic/), including the full log and both classes' text failure reports.

## 6. What the AI did well, missed, and why review is necessary

The AI selected meaningful ordering risks, supplied correct specification-derived monetary expectations, checked actual state as well as interactions, and predicted which cases would fail. It did not change production logic or replace correct expectations with the buggy implementation's results.

However, it did not account for the installed Java/Mockito compatibility problem. Its exact-stock case bundles two requirements and masks the sequence assertion after an earlier failure. All three behaviours already had coverage in the existing repository, so it found no independently new case here. It also covers only ordinary single-threaded outcomes for distinct product IDs: duplicate-SKU aggregation, dependency exceptions, concurrency, and recovery after partial failure remain outside this experiment. Pricing-wide correctness cannot be inferred from these ordering cases.

We would not trust the generated tests without review and execution. Correct-looking source initially failed for an environmental reason, plausible assertions can duplicate existing evidence, and a test that names multiple requirements may not reach all its checks. The useful outcome is documented independent corroboration and a concrete compatibility/review lesson, with no unsupported improvement claim.

## Reproduce

From the root, run the two commands in [README.md](../../README.md) for `without-recorded-ai` and the default suite, saving each report before the next `clean`. Run `AIExperimentKnownDefectsTest` separately with `-Dquickcart.excludedGroups=none`; its two tests are expected to fail on the unchanged implementation. The broader `without-ai` profile is a different exclusion set and was not used for the comparison above.

The original concrete-spy code and its failure log document the compatibility problem. The current tests contain the reviewed interface-delegation adaptation.
