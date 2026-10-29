# Architecture and implemented contracts

## Responsibilities

- Main constructs one Restaurant, TurnController, GameUI and GameController.
- GameController owns the session loop and final scoring. Its currentTurn is the number of completed turns (initially 0). It cannot be set externally. A controller runs one session.
- TurnController owns sequential turns, arrivals, available actions, validation, staff execution and phase timing.
- GameUI formats state and reads numbered choices. It receives Restaurant for display and never mutates it. Action values are the controller's immutable Action records.
- Restaurant coordinates customer registration, seating, order creation, service counts, payment and abandonment.
- Entities validate their local operations. They contain no console I/O.

GameController and TurnController receive concrete dependencies through constructors. This is dependency injection, not a claim of full dependency inversion. No interface is required just to satisfy a SOLID checklist.

## Turn execution

beginTurn requires the next consecutive turn, adds any scheduled arrival, and resets staff once. Repeated or skipped turns are rejected.

The controller generates legal waiter choices and then legal chef choices conditional on the selected waiter task. A newly selected order is included in the chef choices even though it has not been created yet. Both choices are validated before either staff action executes. Invalid input is retried in the UI; invalid programmatic action pairs leave staff and domain state unchanged.

The waiter executes before the chef. An order completed by the chef cannot be served until a subsequent turn. Wait consumes the staff member's action.

After staff execution, eating customers become ready to pay, the cashier processes at most one previously ready customer, and unserved customers lose satisfaction and may abandon. The turn is then recorded as completed. The final result appears only after all 18 turns.

End-of-input stops the session gracefully without declaring victory or defeat. If input ends while choosing, neither staff action has executed. The interrupted session cannot be restarted; construct a fresh game.

## Customer and order lifecycle

Customer states are WAITING -> SEATED -> ORDERED -> READY_TO_PAY -> PAID. Unserved customers may instead become CANCELLED at zero satisfaction. Status mutation is package-private and transition-validated.

Eating is represented by ORDERED plus a SERVED order, so no EATING enum is needed. Customer stores servedTurn and readyToPayTurn. Service on turn t permits readiness on t+1 and payment on t+2. Tables remain occupied throughout eating and payment waiting.

Orders follow PREPARING -> READY -> SERVED -> PAID. Only PREPARING or READY orders may be cancelled. Each chef action adds one unit. Table maintains both ends of its customer association; unAssign clears both references.

Restaurant retains customer registrations after departure to reject repeated IDs. Active-customer snapshots exclude PAID and CANCELLED customers. Seating removes the chosen customer from the waiting queue. Payment validates the oldest queue entry, registered served order and table before changing revenue, statuses, counters, queue and table. Abandonment cancels an unfinished order, removes it and queue membership, releases the table, and counts one departure.

The six-customer bound refers to total arrivals, not simultaneous occupancy. There are exactly two tables. Orders are created only for registered seated customers; closed orders are removed.

## Money, menu and scoring

Prices and revenue are stored in integer cents; dollar getters return double for presentation. MenuItem rejects fractional cents and non-finite prices. Order totals sum integer cents.

Customers choose deterministically by ID: Pasta, Salad, Burger, then repeat. The manager selects actions and customers, never food. This rule supports reproducible runs and makes customer 1 match the manual's Pasta example.

Served count means meals delivered. Paid count is separate. Payment stores a satisfaction total and count, retaining the payment-time value even after the customer leaves. The optional paid average is empty before the first payment; the UI displays N/A.

Victory requires completion of turn 18, at least 4 paid customers, at least 4500 cents, and average paid satisfaction of at least 60.

## Validation

SkeletonTest is retained as the dependency-free regression runner name. It now tests implemented behavior, including complete real-UI replays, rather than stub exceptions. See STAGE1_TEST_NOTES.md for replay instructions and results.