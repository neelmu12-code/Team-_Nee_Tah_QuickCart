# Relevant AI output from the supplied screenshots

The [eight original PNG screenshots are included here](screenshots/README.md). The text below transcribes selected **visible excerpts**, not a reconstruction of the full responses. Section numbers jump because intermediate content was not supplied. Ellipses in attachment labels and cropped screenshot boundaries are not evidence of any particular missing text. Original conversation date/model and any downloadable ZIP contents are not established by the screenshots.

## Response to P1: requirements analysis

> I've gone through all 7 pages of the QuickCart project instructions. The biggest thing to understand is that we are not being asked to build an application or simply write a bunch of JUnit tests. We're being asked to evaluate how trustworthy an existing application is, using evidence from multiple testing techniques.

The visible project-overview table states:

| Field | Visible output |
| --- | --- |
| Course weight | 20% |
| Final deadline | Dec 2, 2026; 10:00 PM |
| Group size | 2 students |
| Recorded demonstration | ~5 minutes |

> That means the project has three big goals:
>
> 1. Design good tests that verify the requirements and uncover potential defects.
> 2. Measure how effective those tests are using coverage, mutation testing, and other techniques.
> 3. Defend our conclusions with actual evidence rather than just claiming the application works.

> One important rule: we generally should not fix the production code. We're evaluating it, not repairing it.
> The instructions also explicitly say to treat this as independent of Assignment 1.

The visible concepts table lists:

| Concept / tool | What it does | Requirement |
| --- | --- | --- |
| Equivalence partitioning | Groups inputs with similar expected behaviour | Required |
| Boundary-value testing | Tests inputs around important limits | Required |
| JUnit 5 | Runs automated functional tests | Required |
| Parameterized testing | Runs tests across different input values | Where appropriate |
| Test doubles / mocking | Replaces a real dependency during testing | At least one meaningful use |
| Integration testing | Tests real components working together | At least one meaningful test |
| JaCoCo | Measures which code is executed | Required |
| PIT mutation testing | Measures whether tests detect small code changes | Required |
| jqwik | Generates inputs to verify general properties | At least one meaningful property |
| AI-assisted testing | Evaluates AI-generated tests against human testing | Required experiment |

### Visible section 13: How I would approach the project

> We have approximately three weeks until December 2. I would organize the work into the following stages.

1. **Understand the QuickCart specification and source code.** Visible explanation: identify classes, requirements, inputs, outputs, and high-risk business logic; decide which tests provide the strongest evidence.
2. **Design and implement JUnit 5 tests.** Visible explanation: use partitions/boundaries, negative cases, parameterized tests, meaningful integration testing, and a justified test double.
3. **Run JaCoCo and PIT.** Visible explanation: establish initial results, investigate missing branches/survivors, improve tests, and capture before-and-after evidence.
4. **Implement jqwik and evaluate AI-generated tests.** Visible explanation: implement a meaningful generated-input property and run a controlled AI experiment with saved prompts, outputs, and comparative results.
5. **Compile evidence and record the demonstration.** Visible explanation: document two defects, assess residual risk, prepare the Evidence Sheet, verify the repository runs, and rehearse the video.

The numbered explanations above are summaries of the visible text, not verbatim quotations.

> The instructions don't require a particular number of test cases, a specific coverage percentage, or a perfect mutation score. This is important because our time should go toward high-value tests with clear reasoning, not simply generating hundreds of assertions.

## Response to P2: proposed skeleton

The visible Explorer diagram proposes this layout (indentation transcribed; descriptive arrows are retained):

```text
Team##_XXX_YYY_QuickCart/
  .vscode/
    extensions.json
    settings.json
  src/
    main/
      java/  <- supplied QuickCart source
      resources/
    test/
      java/
        PricingTest.java
        CouponTest.java
        OrderIntegrationTest.java
        PaymentMockTest.java
        CartPropertyTest.java
      resources/
  evidence/
    coverage/
    mutation/
    ai/
      prompts.md
      relevant-output.md
    strategy.md
    defects.md
    project-evidence-sheet.md
    demo-outline.md
  pom.xml
  README.md
  .gitignore
```

> This follows the required repository structure on page 4 of your instructions, with additional organisational files to make our work easier.

The visible status checklist marks VS Code structure, Maven configuration, JUnit/Mockito starter templates, JaCoCo/PIT configuration, and evidence/video templates as prepared. It marks actual QuickCart source, specification-based tests, and real coverage/mutation/defect evidence as still needed. These are the historical AI response's claims; the screenshots alone do not verify the contents of an archive or any generated files.

> I deliberately left the JUnit templates disabled and the jqwik property unimplemented, rather than inventing QuickCart classes or tests that appear to pass without testing anything. I've checked the ZIP and XML structure, but I haven't run a Maven build of the project.

> The next thing I need is the actual QuickCart source code and behavioural specification provided by your professor. With those, we can transform this from a starter skeleton into a real functioning test project, working through every class and required testing concept together.

## Scope of this evidence

The supplied output helps explain planning and the proposed structure. It contains no visible executable test bodies, test-run results, or mutation results. The named starter test files do not match every final test filename, and the diagrams' optional Evidence Sheet/video templates are not claimed to exist. These excerpts therefore support disclosure of AI use, while the separately recorded [test-generation experiment](candidate-output.md) supplies executable candidate output and [evaluation](evaluation.md).
