# Architecture and UML mapping

## BCE dependencies

```text
Main (composition root)
  -> GameUI implements GameView
  -> GameController -> TurnController -> entities
         -> GameView (port) <- GameUI
         -> GameState (immutable presentation data)
```

`GameUI` formats snapshots and parses input. `GameController` will own the session loop, final result and turn counter. `TurnController` will coordinate phases and staff actions. Entities hold state and local behavior without console or controller dependencies. Controllers receive the view through an interface, so tests can replace the console with a small fake.

The UML's UI/controller dependency is implemented as a control-owned port instead of coupling the controller to the concrete console. `GameState` ensures the boundary receives values, not mutable entities. When implementing actions, extend the snapshot with immutable options/IDs and let controllers resolve selections to entities.

## SOLID in this scaffold

- **Single responsibility:** presentation, turn orchestration, session flow and individual domain objects have separate files and responsibilities.
- **Open/closed:** another boundary can implement `GameView` without modifying domain logic. Introduce small rule policies when actual alternative rules exist.
- **Liskov substitution:** `GameUI` and test views fulfill the same `GameView` contract. There is no artificial staff inheritance hierarchy.
- **Interface segregation:** `GameView` exposes only game presentation/input operations; entities do not depend on it. Split input/output ports if future clients need only one side.
- **Dependency inversion:** the controller depends on `GameView`, injected by `Main`, rather than constructing a console boundary. Stable entities remain concrete; an interface for every class is unnecessary.

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
| `displayGameState()` without parameters | Receives an immutable `GameState` snapshot, avoiding a boundary reference to mutable Restaurant. |
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
- Collection membership is protected by immutable lists, but entity objects remain mutable within the domain. The boundary only receives immutable snapshots.
- `startGame()` displays state only. `runTurn`, `isVictory` and no-argument satisfaction decay throw `UnsupportedOperationException`. Private turn-phase methods are TODO outlines, not implemented phases.

The scaffold is single-threaded. Status transitions and complete seat/order/departure transactions need the remaining control workflows and tests; this is not a finished game engine.
