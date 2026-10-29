# Restaurant Rush — SC2002 Stage 1

A Java command-line restaurant management project organised using Boundary–Control–Entity (BCE). This README and the [task allocation](docs/TASKS.md) cover **Stage 1: Basic restaurant operation only**, based on Sections 2, 3 and 4.1 of the supplied *SC2002 Restaurant Rush Group Assignment* manual.

Repository: [rejudithnate/SC2002_GroupAssignment](https://github.com/rejudithnate/SC2002_GroupAssignment).

**Current status: playable Stage 1 with behavioral regression checks.** The console game runs all 18 turns with manager-selected staff tasks, automatic payments, abandonment and final scoring. Only Regular customers and basic restaurant operation are included. See [verification notes](docs/STAGE1_TEST_NOTES.md) for reproducible victory and defeat runs; team review and release tagging remain pending.

## Required Stage 1 base setting

| Setting | Requirement |
| --- | --- |
| Duration | Always run all 18 turns; no early victory or defeat |
| Tables | 2; remain occupied until payment or abandonment |
| Arrivals | One Regular customer at the start of turns 1, 4, 7, 10, 13 and 16; six total |
| Initial satisfaction | 100 per customer |
| Waiter | At most one task per turn: seat, take an order, serve a READY order, or Wait |
| Chef | At most one preparation unit on one selected order per turn, or Wait |
| Cashier | Automatically processes at most one eligible payment per turn, oldest READY_TO_PAY first |
| Victory | At the end of turn 18, **at least 4 paid customers AND revenue of at least $45 AND average paid-customer satisfaction of at least 60** |
| Defeat | Any victory condition is unmet after turn 18 |

| Menu item | Price | Preparation units |
| --- | --- | --- |
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |

Customers choose their own food using a documented deterministic rule or seeded randomness. The manager chooses service and kitchen priorities, never the food. Show the customer's selection when the order is placed. All mandatory functionality must work offline. Display money with two decimals; if fractional prices arise, the manual requires integer-cent storage.

## Turn sequence and timing

At the start of each turn, display the turn number, revenue, average satisfaction of paid customers, waiting queue, occupied tables, each customer's satisfaction, order statuses and available staff tasks.

1. **Arrivals:** add the scheduled customer with satisfaction 100.
2. **Manager choices:** collect the Waiter's task and Chef's task. Invalid choices explain the failure and allow another choice without consuming the staff task.
3. **Kitchen and serving:** execute the Waiter before the Chef. The Chef can work on an order taken this turn. An order completed by the Chef becomes READY, but cannot be served until a later turn because the Waiter has already acted. Serving marks the order SERVED.
4. **Eating and payment:** a customer served on an earlier turn finishes one full turn of eating and becomes READY_TO_PAY. They cannot pay in that same turn. The Cashier charges at most one customer who was already READY_TO_PAY, oldest first. Payment records revenue, marks both customer and order PAID, records satisfaction and frees the table.
5. **Waiting and abandonment:** every waiting or seated Regular customer who has not been served loses 8 satisfaction, floored at 0. At 0, the customer leaves without paying, their unfinished order is cancelled and their table is freed. Served, eating and READY_TO_PAY customers lose nothing.
6. **Result:** finish the turn and, only after turn 18, evaluate all three victory conditions.

At each turn end, show served count, unhappy departures, revenue, average satisfaction of paid customers and turns used. Compute the average from satisfaction recorded at payment, not from customers currently in the restaurant. Display **N/A** before anyone has paid. Track paid customers explicitly for the victory check; document how the displayed served count is defined.

The manual's Pasta example is a timing check: seat on turn 1 (satisfaction 92), take the order and prepare unit 1 on turn 2 (84), complete preparation on turn 3 (76), serve on turn 4, become READY_TO_PAY on turn 5, and pay $18 on turn 6 with satisfaction 76. The turn-16 arrival cannot pay by turn 18, so at most five customers can pay in this base setting.

## Implemented behavior

- Choose a displayed task number for each staff member; 0 always means Wait. Both choices are collected before actions execute.
- Customers select Pasta, Salad and Burger in a repeating cycle by customer ID. The manager never selects food.
- Money is stored in integer cents. Payment records satisfaction once and clears the payment queue entry and table.
- Served counts meals delivered; paid count is separate. Before anyone pays, the average displays N/A.
- Invalid input is retried. End-of-input stops gracefully without displaying an unfinished game's result.
- Each controller represents one session. Create a new controller and restaurant to play again.

The [architecture notes](docs/ARCHITECTURE.md) describe the implemented contracts and timing. The [test notes](docs/STAGE1_TEST_NOTES.md) document the manual's Pasta timeline and complete game replays.

## Build and run

Install a **JDK 17 or newer** and ensure `java` and `javac` are on your PATH. No Maven, Gradle or third-party libraries are needed.

From the repository root in PowerShell:

```powershell
.\scripts\build.ps1 -Run
.\scripts\build.ps1 -Test
```

Use `-Test -Run` to do both. If Windows blocks local scripts, run `powershell -ExecutionPolicy Bypass -File .\scripts\build.ps1 -Test -Run` (applies only to that process).

On macOS/Linux:

```sh
mkdir -p build/classes
find src/main/java src/test/java -name '*.java' > build/sources.txt
javac --release 17 -Xlint:all -d build/classes @build/sources.txt
java -cp build/classes restaurantrush.SkeletonTest
java -cp build/classes restaurantrush.Main
```

In an IDE, mark `src/main/java` as source and `src/test/java` as test source, select Java 17+, and run `restaurantrush.Main`. The test runner is a plain Java main class; it does not require JUnit.

## Structure

```text
src/main/java/restaurantrush/
  Main.java                    dependency wiring only
  boundary/GameUI.java         console parsing and presentation
  control/                     GameController, TurnController
  entity/                      Restaurant, Customer, Table, Order, Menu, MenuItem,
                               Waiter, Chef, Cashier, CustomerStatus, OrderStatus
src/test/java/restaurantrush/   behavioral regression checks
scripts/build.ps1              compile, test and run
docs/                         architecture, UML decisions and team tasks
```

## Five-person Stage 1 allocation

| Contributor | Primary files | Stage 1 responsibility |
| --- | --- | --- |
| 1 — Boundary & integration | `boundary/`, `Main.java` | Complete CLI, status displays, input retries, result presentation and dependency wiring |
| 2 — Game flow | `GameController`, `TurnController` | Exact six-step sequence, 18-turn loop, eligibility timing and victory check |
| 3 — Restaurant & customers | `Restaurant`, `Customer`, `Table`, `CustomerStatus` | Customer/table lifecycle, queues, satisfaction decay, payment statistics and departure cleanup |
| 4 — Orders & menu | `Order`, `Menu`, `MenuItem`, `OrderStatus` | Required menu, deterministic/seeded food selection, preparation and order lifecycle |
| 5 — Staff & verification | `Waiter`, `Chef`, `Cashier`, build script, test runner | Staff actions and limits, integration checks, reproducible victory/defeat evidence and release verification |

Replace contributor numbers with team names when agreed. Everyone tests their own module; contributor 5 coordinates integration checks. Announce shared signature changes and obtain review from the affected owner. See [CONTRIBUTING.md](CONTRIBUTING.md) for the branch/PR workflow and [TASKS.md](docs/TASKS.md) for detailed checklists, handoffs and acceptance criteria.

## Stage 1 completion evidence

Stage 1 must compile and run independently at the Git tag `stage-1`. Create that tag only after the complete Stage 1 game and its evidence pass review; the skeleton is not that milestone.

| Evidence | Current status / required contents |
| --- | --- |
| Runnable `stage-1` release | Pending: verify a clean checkout builds and runs offline |
| Reproducible victory run | Verified locally: 5 paid, $69.00, average 74.4; see test notes for all actions and baseline commit |
| Reproducible defeat run | Verified locally: Wait every turn; 0 paid, $0.00, N/A average; all targets unmet |
| Test notes | Available at `docs/STAGE1_TEST_NOTES.md`; include the manual's Pasta timeline, edge cases and both complete runs |

Preserve the Stage 1 source snapshot and test notes in Git. This scope does not include later-stage implementation or the full-assignment submission package.
