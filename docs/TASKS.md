# Stage 1 task allocation

Scope: **Basic restaurant operation only**, using Sections 2, 3 and 4.1 of the supplied *SC2002 Restaurant Rush Group Assignment* manual. The [README](../README.md) contains the required rules. These are outstanding implementation tasks; no checkbox implies the skeleton already meets a gameplay requirement. Later-stage features and the full submission package are excluded from this allocation.

## Ownership and shared contracts

Use contributor numbers until names are assigned. Each contributor owns their source files and module tests. Contributor 5 coordinates the shared test runner and release evidence; they are not responsible for writing everyone else's tests.

| Contributor | Owned source files | Required coordination |
| --- | --- | --- |
| 1 — Boundary & integration | `boundary/GameUI.java`, `Main.java` | Review GameUI/controller method signatures with 2; wire menu data from 4 |
| 2 — Game flow | `control/GameController.java`, `TurnController.java` | Coordinate state changes with 3/4 and staff timing with 5 |
| 3 — Restaurant & customers | `entity/Restaurant.java`, `Customer.java`, `Table.java`, `CustomerStatus.java` | Provide atomic lifecycle/statistics methods to 2/5 and order attachment contracts to 4 |
| 4 — Orders & menu | `entity/Order.java`, `Menu.java`, `MenuItem.java`, `OrderStatus.java` | Agree customer food-selection API with 3 and order-action contracts with 2/5 |
| 5 — Staff & verification | `entity/Waiter.java`, `Chef.java`, `Cashier.java`, `scripts/build.ps1`, shared test runner | Verify staff timing with 2 and payment cleanup with 3; integrate everyone's tests |

Keep console I/O in the boundary, orchestration in controllers, and state/invariants in entities. Main passes GameUI directly to GameController. GameUI reads Restaurant getters for display and must not mutate domain state. Use abstractions only when they serve an actual responsibility, and keep the UML aligned with implemented changes. Do not add inheritance or interfaces just to tick a SOLID box.

## 1 — Boundary and integration

- [ ] Display start-of-turn number, revenue, paid-customer satisfaction average, waiting queue, occupied tables, customer satisfaction, order statuses and available staff tasks.
- [ ] Offer Waiter choices to seat a selected queued customer, take a selected seated customer's order, serve a selected READY order, or Wait. Offer Chef choices to prepare a selected order or Wait.
- [ ] Show food selection once the customer places the order. Never ask the manager to choose the customer's food.
- [ ] Retry malformed input and display controller/domain rejection reasons without consuming staff actions. Handle end-of-input gracefully.
- [ ] Display end-of-turn served count, unhappy departures, revenue, paid-customer average and turns used. Display `N/A` if no one has paid and money with two decimals.
- [ ] Display Victory/Defeat only after turn 18, including the three final scoring metrics.
- [ ] Wire dependencies and the mandatory base setting in Main; retain offline build/run instructions.
- [ ] Test input retries, empty action lists, target selection, `N/A`, money formatting and final-result output.

**Handoff:** contributors 1 and 2 agree GameUI/controller method signatures, selectable IDs and the validation-result format before building menus. Keep all game-rule decisions out of GameUI.

## 2 — Game flow

- [ ] Implement all 18 turns with no early exit for winning or losing. Keep turn advancement in the session loop.
- [ ] Trigger one arrival on turns 1, 4, 7, 10, 13 and 16 through contributor 3's lifecycle API.
- [ ] Follow the manual's six steps: arrivals; manager choices; kitchen/serving; eating/payment; waiting/abandonment; result.
- [ ] Reset staff once per turn, execute the Waiter before the Chef, and permit the Chef to work on an order just taken by the Waiter.
- [ ] Prevent serving an order completed by the Chef in the same turn.
- [ ] Track service/payment eligibility turns (or equivalent state): served on turn `t`, READY_TO_PAY no earlier than `t + 1`, payment no earlier than `t + 2`.
- [ ] Automatically process at most one previously READY_TO_PAY customer, oldest first. Do not accidentally charge a customer newly added to that queue this turn.
- [ ] Apply end-of-turn decay only to unserved waiting/seated customers, then process abandonment and cleanup.
- [ ] Check victory only after turn 18: paid count >= 4, revenue >= $45, and paid-customer average >= 60, all required.
- [ ] Implement validated target/action dispatch and repeat invalid choices without using the staff task.
- [ ] Replace controller stub-exception tests with turn-order, timing and final-result behavior tests.

**Handoff:** ask contributor 3 for lifecycle operations instead of independently mutating counters/queues. Coordinate payment and action limits with contributor 5. Revisit existing private phase names/order where necessary to match the manual.

## 3 — Restaurant and customers

- [ ] Support exactly two tables and six scheduled Regular arrivals, each starting at satisfaction 100. Contributor 2 owns the arrival schedule; this module owns registration/state.
- [ ] Allow a selected queued customer to be seated and removed from the waiting queue; keep both sides of the table/customer association consistent.
- [ ] Keep a table occupied until payment or abandonment, including while eating and waiting to pay.
- [ ] Enforce valid customer lifecycle transitions and expose timing state needed by contributor 2. The current enum has no EATING value; document the chosen representation without changing the required delays.
- [ ] Reduce eligible Regular customer satisfaction by 8 at turn end, minimum 0; served/eating/READY_TO_PAY customers lose nothing.
- [ ] At satisfaction 0, record one unhappy departure, cancel unfinished orders with contributor 4's API, remove queue membership and free the table. Do not add revenue.
- [ ] Complete payment consistently: mark customer/order PAID, record revenue and payment-time satisfaction once, remove payment-queue membership and free the table.
- [ ] Store paid count and payment-time satisfaction history/total independently of active customers. Return an explicit no-payments state for the UI's `N/A`.
- [ ] Agree and document served-count terminology with 1/2/5; never use a meal-served count as a substitute for the required paid count.
- [ ] Test selected-customer queue removal, table ownership, abandonment, duplicate-payment prevention and paid-only averages after customers leave.

**Handoff:** contributor 5's Cashier calls the payment operation; contributor 2 controls when it may run. Coordinate order/customer attachment with 4 so rejected operations cannot leave partially updated state.

## 4 — Orders and menu

- [ ] Supply Salad ($9, 1 unit), Burger ($15, 1 unit) and Pasta ($18, 2 units).
- [ ] Define and document a deterministic customer selection rule or fixed random seed. Expose it through customer/domain behavior with contributor 3; the manager never selects food.
- [ ] Make the selected food available for display when the order is placed. Record the rule/seed in reproducible test notes.
- [ ] Implement and validate PREPARING -> READY -> SERVED -> PAID, with cancellation for abandoned unfinished orders.
- [ ] Add exactly one preparation unit for a Chef action; only become READY when required units are complete.
- [ ] Agree a validated order creation/registration/attachment workflow with 2/3/5. Keep bills out of revenue until payment.
- [ ] Keep Stage 1 pricing consistent across orders and revenue. If fractional prices are introduced, use integer cents as required by the manual.
- [ ] Test all menu values, Pasta's two units, illegal transitions, cancellation and repeatable customer choices.

**Handoff:** contributor 3 owns Customer.java; coordinate changes there instead of editing it concurrently. Six refers to total scheduled arrivals; do not invent an additional gameplay restriction from the scaffold's order-capacity constant.

## 5 — Staff and verification

- [ ] Complete Waiter seating, order-taking, serving and Wait actions, each consuming at most one task per turn.
- [ ] Complete Chef preparation and Wait actions, limited to one unit on one order per turn.
- [ ] Ensure rejected actions do not consume availability or partially mutate state.
- [ ] Complete automatic Cashier payment through contributor 3's atomic operation; coordinate the one-payment limit and oldest-eligible selection with 2.
- [ ] Integrate module tests and add deterministic end-to-end timing checks. Update tests that currently expect unfinished methods to throw.
- [ ] Create `docs/STAGE1_TEST_NOTES.md` with a reproducible victory run and a reproducible defeat run: commit/tag, selection rule/seed, every manager action, final paid count, revenue, paid-customer average and result.
- [ ] Verify a clean checkout builds and runs offline with the documented commands.
- [ ] Coordinate team review of the completed Stage 1 commit, then create the runnable `stage-1` tag. Do not tag the current scaffold as the finished stage.

**Handoff:** each owner supplies tests for their module; coordinate changes to the shared runner to avoid merge conflicts. Contributor 1 reviews launch instructions and CLI replay steps, and contributor 2 verifies complete-run results.

## Acceptance checks

| Scenario | Required result | Lead / reviewer |
| --- | --- | --- |
| Manual Pasta timeline | Turns 1–6: seat, take order + unit 1, unit 2/READY, SERVED, READY_TO_PAY, PAID; satisfaction 92, 84, 76, 76, 76, recorded 76; revenue $18 only at payment | 5 / 2 |
| Invalid action | Explain failure, retry, no staff task or partial state consumed | 1 / 5 |
| Queue priority and full tables | Manager can select which waiting customer to seat; occupied tables cannot be reused | 3 / 1 |
| Same-turn timing | Chef may start a newly taken order; cannot serve a newly completed dish or pay a newly READY_TO_PAY customer | 2 / 5 |
| Abandonment | At 0 satisfaction, cancel unfinished order, free table, remove customer and record one departure with no revenue | 3 / 4 |
| Paid-only average | `N/A` before payment; retain payment-time satisfaction after departure; exclude unpaid customers | 3 / 1 |
| Staff limits | One Waiter task, one Chef unit, one automatic Cashier payment per turn; oldest eligible payment first | 5 / 2 |
| Base arrivals and duration | Exactly six arrivals; all 18 turns run; turn-16 customer cannot pay; no more than five pay | 2 / 5 |
| Victory boundaries | All three thresholds required; test exact thresholds and failure of each independently | 2 / 3 |
| Complete runs | One reproducible victory and one defeat from the same documented base configuration | 5 / all |

## Integration order and remaining choices

1. **Agree contracts:** 1/2 define choices and GameUI display arguments; 2/3/5 define service/payment timing and lifecycle operations; 3/4 agree customer food selection and order creation.
2. **Implement modules:** work in separate branches on owned files and add module checks. Coordinate shared test-runner edits with 5.
3. **Integrate a customer lifecycle:** reproduce the manual's Pasta example before testing the six-arrival game.
4. **Complete the game:** add the full arrival schedule, priorities, abandonment, all 18 turns and final scoring; replay both endings.
5. **Release:** complete test notes, update README status and architecture/UML notes to match actual code, verify a clean offline checkout, and tag the reviewed commit `stage-1`.

The team still chooses member names, action IDs, the reproducible food-selection rule/seed, timing-state representation and the displayed served-count definition (the manual's victory target explicitly uses paid customers). These are design choices; the manual's arrival schedule, prices, decay, delays and victory thresholds are already fixed.
