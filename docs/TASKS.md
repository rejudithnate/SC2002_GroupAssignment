# Team implementation backlog

## Agree before implementing gameplay

Record these answers from the assignment specification; the UML does not define them:

- Customer arrival turns/count and meaning of the six-customer multiplicity.
- Three menu items, prices, preparation units and how customers choose quantities/items.
- Waiter/chef action IDs, legal targets, skip behavior and whether invalid choices consume a turn.
- Exact turn-phase ordering and when staff reset.
- Satisfaction decay per state, unhappy-departure threshold and final average population.
- Eating duration and when a served customer becomes READY_TO_PAY.
- Cashier capacity, payment timing and meaning of the served-customer statistic.
- Victory conditions, behavior after turn 18 and any early game-over condition.

## Suggested integration order

1. **Contributors 1 + 2:** agree action choices and immutable target IDs in `GameView`/`GameState`.
2. **Contributors 3 + 4:** agree entity transitions, menu data and order construction. Validate cross-entity operations together so rejected actions leave no half-created order or inconsistent queue.
3. **Contributor 5:** complete staff behavior and extend tests for invalid actions/per-turn limits.
4. **Contributor 2 with 3 + 5:** implement turn phases. Reset staff once per turn and process each payment/departure exactly once. Add deterministic scenario tests.
5. **Contributors 1 + 2:** wire choices and complete the 18-turn loop. Only the session loop advances `currentTurn`; `startTurn` processes its current value.
6. **Everyone:** verify full games, invalid input, full tables, empty queues, cancellations, payment cleanup and win/loss reporting. Replace stub-exception checks with behavior tests.

## Completion checklist by owner

### 1 — Boundary & integration

- [ ] Display waiter/chef action menus and immutable target options.
- [ ] Handle end-of-input gracefully at the application boundary.
- [ ] Wire actual menu/configuration in Main.
- [ ] Exercise the console with invalid input and a full game.

### 2 — Game flow

- [ ] Implement phase ordering and action dispatch in TurnController.
- [ ] Implement turn advancement and end conditions in GameController.
- [ ] Implement `isVictory()` with agreed scoring.
- [ ] Present final results through GameView.

### 3 — Restaurant & customers

- [ ] Enforce customer status transitions and aggregate consistency.
- [ ] Implement arrivals, queue cleanup and atomic departure recording.
- [ ] Implement satisfaction decay and agreed average population.
- [ ] Keep table/customer references consistent after departures.

### 4 — Orders & menu

- [ ] Supply actual three-item menu values.
- [ ] Confirm preparation aggregation and cancellation rules.
- [ ] Validate selections and create/register/attach orders together.
- [ ] Clarify order capacity, retention and customer limits.

### 5 — Staff & quality

- [ ] Confirm staff action limits and payment capacity.
- [ ] Coordinate eating/payment state with the turn owner.
- [ ] Add deterministic full-turn/game contract checks.
- [ ] Keep build instructions working on teammates' JDKs.
