# Restaurant Rush — SC2002

A Java skeleton based on the supplied Restaurant Rush UML, organised using Boundary–Control–Entity (BCE). It includes all 12 UML classes and both enums, a console entry point, and dependency-free contract checks.

**This is a scaffold, not a playable game.** Running it displays the initial state. Turn sequencing, action selection, arrivals, satisfaction rules, eating duration and victory conditions are deliberately unfinished. Unimplemented public gameplay methods throw explicit exceptions rather than silently pretending to work.

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
  control/                     GameController, TurnController, GameView, GameState
  entity/                      Restaurant, Customer, Table, Order, Menu, MenuItem,
                               Waiter, Chef, Cashier, CustomerStatus, OrderStatus
src/test/java/restaurantrush/   contract checks
scripts/build.ps1              compile, test and run
docs/                         architecture, UML decisions and team tasks
```

## Five-person collaboration

| Contributor | Primary files | First deliverable |
| --- | --- | --- |
| 1 — Boundary & integration | `boundary/`, `Main.java` | Action menus and validated input |
| 2 — Game flow | `GameController`, `TurnController`, `GameView`, `GameState` | Turn sequence and session loop |
| 3 — Restaurant & customers | `Restaurant`, `Customer`, `Table`, `CustomerStatus` | Arrivals, seating, departures and satisfaction |
| 4 — Orders & menu | `Order`, `Menu`, `MenuItem`, `OrderStatus` | Actual three-item menu and order rules |
| 5 — Staff & quality | `Waiter`, `Chef`, `Cashier`, build script, test runner | Staff actions and end-to-end checks |

Ownership is a coordination aid, not a restriction. Everyone should test their own changes. Announce changes to shared method signatures and get the affected owner to review them before merging. See [CONTRIBUTING.md](CONTRIBUTING.md) for the branch/PR workflow, [architecture](docs/ARCHITECTURE.md) for BCE/SOLID guidance, and [tasks](docs/TASKS.md) for implementation order and unresolved rules.
