# QuickCart Software Testing and Quality Evaluation

Team members: **Neel Upadhyay** and **Taha Qureshi**. Team number: **not yet assigned**.

This project evaluates the supplied QuickCart implementation against [PROJECT_SPECIFICATION.md](PROJECT_SPECIFICATION.md). Production Java content is unchanged from the starter. Correct specification regressions deliberately expose defects; they are kept separate from the passing baseline used by PIT.

## Run the project

Use Java 17+ and Maven from this directory. Validation was performed with Maven 3.10.0 and Eclipse Adoptium Java 25.0.2; historical reports identify their original Java 21/Maven environment separately. Dependencies must be available locally for offline execution.

```powershell
mvn clean test
mvn clean test jacoco:report
mvn test-compile org.pitest:pitest-maven:mutationCoverage
```

The default tests and PIT baseline should pass. Four jqwik property methods execute 800 generated checks, counted as four tests in Surefire.

Latest reviewed treatment: **52 passing tests**, **77/77 lines and 52/52 branches covered**, and **27/31 PIT mutants killed (87.1%)**. The recorded control had 51 passing tests and the same coverage/mutation results. A separate known-defect run produced 14 expected failures. See the AI evaluation for raw evidence and the distinction between defects, compatibility errors, and passing baselines.

Open `target/site/jacoco/index.html` for current coverage and `target/pit-reports/index.html` for current mutation results. `clean` removes `target/`; durable snapshots are saved under `evidence/`.

Run the deliberately failing specification regressions separately:

```powershell
mvn test "-Dquickcart.excludedGroups=none" "-Dgroups=known-defect" "-Djacoco.destFile=target/jacoco-known-defects.exec"
```

This command is expected to exit with code 1 on the unchanged implementation. The `known-defect` tag is excluded from the normal suite, and both known-defect classes are excluded from PIT because its unmutated baseline must pass. The separate JaCoCo data file keeps diagnostic coverage apart from passing-suite data. Quote Maven `-D` arguments in PowerShell as shown.

## Evidence map

| Evidence | Location |
| --- | --- |
| Testing strategy, risks, partitions, properties and oracles | [evidence/strategy.md](evidence/strategy.md) |
| Two significant defects, reproduction and impact | [evidence/defects.md](evidence/defects.md) |
| Historical coverage snapshot and interpretation | [evidence/coverage/README.md](evidence/coverage/README.md) |
| Historical mutation before/after reports and survivor analysis | [evidence/mutation/README.md](evidence/mutation/README.md) |
| Supplied historical AI prompts | [evidence/ai/prompts.md](evidence/ai/prompts.md) |
| Eight original screenshots, displayed in order | [evidence/ai/screenshots/README.md](evidence/ai/screenshots/README.md) |
| Visible historical AI output excerpts | [evidence/ai/relevant-output.md](evidence/ai/relevant-output.md) |
| Newly recorded AI-generated candidate tests | [evidence/ai/candidate-output.md](evidence/ai/candidate-output.md) |
| AI experiment design, review, measurements and limitations | [evidence/ai/evaluation.md](evidence/ai/evaluation.md) |
| Overall quality and residual risks | [evidence/quality-assessment.md](evidence/quality-assessment.md) |

Historical reports retain their measured dates, revisions and counts. Their original `student/` module paths are provenance; current runnable sources are under root-level `src/`.

## Reproduce the newly recorded AI comparison

The control excludes only the three new candidates from the recorded experiment. It retains all pre-existing tests, including `AIOrderServiceTest` and `AITest`.

```powershell
mvn -Pwithout-recorded-ai clean test org.pitest:pitest-maven:mutationCoverage
```

Save the reports, then run the treatment with the same tools and production code:

```powershell
mvn clean test org.pitest:pitest-maven:mutationCoverage
```

The treatment includes the new passing candidate. Its two defect-revealing candidates are in `AIExperimentKnownDefectsTest`, excluded from both passing baselines and measured separately:

```powershell
mvn test "-Dtest=AIExperimentKnownDefectsTest" "-Dquickcart.excludedGroups=none" "-Djacoco.destFile=target/jacoco-ai-diagnostics.exec"
```

This diagnostic command is expected to fail. The broader, pre-existing `-Pwithout-ai` profile excludes `AIOrderServiceTest` and the newly recorded experiment classes; it still retains `AITest`. It is a different control from `without-recorded-ai` and must not be substituted when interpreting the saved recorded-experiment comparison.

The team will prepare the Evidence Sheet and recorded demonstration separately.
