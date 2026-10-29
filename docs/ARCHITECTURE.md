# Architecture and UML mapping

## BCE dependencies

```text
Main (composition root)
  -> GameUI (console input/output)
  -> GameController -> TurnController -> entities
         -> GameUI -> Restaurant getters (display only)
```

`GameUI` formats restaurant state and parses input. `GameController` will own the session loop, final result and turn counter. `TurnController` will coordinate phases and staff actions. Entities hold state and local behavior without console or controller dependencies. Main constructs GameUI and passes it directly to GameController. Tests supply a Scanner and captured PrintStream to exercise the real UI without an interactive console.

The skeleton uses the original UML classes without an additional view interface or state-transfer class. GameController calls GameUI directly, passing Restaurant and turn numbers to `displayGameState`. This display method only reads getters; game mutations stay in control/entity operations. This is a coding convention rather than compiler-enforced read-only access. The concrete controller-to-UI call and display arguments should be reflected when synchronising the UML with the code.

## SOLID in this scaffold

- **Single responsibility:** presentation, turn orchestration, session flow and individual domain objects have separate files and responsibilities.
- **Open/closed:** domain rules are separate from console formatting, but replacing the concrete UI would require changing controller dependencies. Do not claim interchangeable boundaries in this version.
- **Liskov substitution:** no custom inheritance or interchangeable implementations are introduced; there is no concrete example of this principle to claim yet.
- **Interface segregation:** there are no custom interfaces in this skeleton. Keep each class's public methods focused rather than adding an interface just for the checklist.
- **Dependency inversion:** Main injects concrete dependencies so construction is separate from use. This is dependency injection, not a full example of DIP: GameController depends directly on GameUI.

## Deliberate UML corrections

| UML detail | Implementation and reason |
| --- | --- |
| `Chef.wait()` / `Waiter.wait()` | `skipTurn()`; Java's no-argument `Object.wait()` is final and is for thread synchronisation. |
| Duplicate `resetForNewTurn()` on Waiter | One method. |
| `Waiter.isAvailable(): void` | Returns `boolean`, matching Chef and its intended use. |
| Order `menuItem: List<Menu>` | `List<MenuItem>`; a purchase contains items, not entire menus. |
| Public Order fields | Private fields with accessors and validated transitions to protect invariants. |
| Revenue `int`, prices `double` | Revenue is `double` so fractional prices are retained. For real monetary software, agree integer cents or `BigDecimal` consistently across all price APIs. |
| Stored `AvgSatisfaction: int`, getter `double` | Derived `double` average avoids stale duplicated state. Population is provisional; see below. |
| `getServedCustomers(customer_id, customerstatus)` and counter setters | Read-only counter getters and named recording operations. Payment increments served count; `recordUnhappyDeparture()` increments departures. |
| `displayGameState()` without parameters | Receives Restaurant, current turn and maximum turns explicitly, so GameUI can display state without storing a controller reference. It must use getters only. |
| Missing constructors/getters | Added to initialise dependencies and access state without public fields. |
| Table/customer association | `Table` updates both ends. `Customer.setTable` delegates; moving an already-seated customer requires unseating first. |
| Two tables, three menu items, up to six orders | Two tables are created; Menu validates exactly three entries; Restaurant permits up to six registered orders. Closed orders can be removed to free slots. |
| Ambiguous `0..6` customer relationship | Not enforced as a total customer limit: the diagram also depicts one customer per table. Confirm whether six means simultaneous or total arrivals. |
| Customer dependency on Waiter/Menu | Controller coordinates selection/actions so Customer does not initiate staff workflows or own the menu. |

## Implemented contracts and provisional assumptions

- Staff may act once between resets; skipping consumes the action. Failed state validation does not consume it.
- Preparing an order adds one unit; required units are the sum of item preparation units. Confirm this rule against the assignment.
- Order lifecycle is `PREPARING -> READY -> SERVED -> PAID`; an unpaid order can be cancelled. Closed orders cannot be prepared, served or paid again.
- Customer starts WAITING with satisfaction 100. `setStatus` currently accepts any enum value; transition restrictions remain a task. The enum has no EATING state: represent eating through a served order and agreed timing, or agree a UML amendment.
- A customer has at most one order. The controller attaches and registers it before `Waiter.takeOrder`. Validate that multi-step operation together in the completed workflow.
- Waiting/payment queues use FIFO. Payment peek is non-destructive; remove the customer only after success. Seating and departure queue cleanup remain controller responsibilities.
- Payment validates a registered served order and READY_TO_PAY status, marks order/customer paid, then records revenue and served count once. Whether “served” means meals delivered instead is an open decision.
- Average satisfaction covers distinct waiting, seated and payment-queue customers; an empty restaurant reports 0. Lifetime/final scoring is unspecified. Do not use this provisional metric for victory until agreed.
- Collection membership is protected by immutable lists, but entity objects remain mutable. The boundary receives Restaurant for display and must not call its mutation methods.
- `startGame()` displays state only. `runTurn`, `isVictory` and no-argument satisfaction decay throw `UnsupportedOperationException`. Private turn-phase methods are TODO outlines, not implemented phases.

The scaffold is single-threaded. Status transitions and complete seat/order/departure transactions need the remaining control workflows and tests; this is not a finished game engine.
