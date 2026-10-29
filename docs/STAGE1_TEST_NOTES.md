# Stage 1 verification notes

Verified on 2026-10-30 against the working-tree implementation based on commit 9f1616db795f0ffe0567381182dfb77c6136d915. These changes are not yet committed or tagged. Record the final merge commit when creating the reviewed Stage 1 release.

## Commands and evidence

From the repository root:

~~~powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build.ps1 -Test
Get-Content build/victory-input.txt | java -cp build/classes restaurantrush.Main
Get-Content build/defeat-input.txt | java -cp build/classes restaurantrush.Main
~~~

Compilation with Java 17 compatibility and all lint checks passes. The regression runner passes 109 behavioral assertions and writes both replay inputs and a victory action trace under ignored build/. The real Main entry point was also exercised with the victory replay.

Coverage includes menu values, cent precision, table associations, staff limits, rejected action pairs, selected-customer seating, full tables, duplicate arrivals, abandonment, immutable snapshots, payment FIFO, one automatic payment per turn, payment-time averages, exact victory thresholds, input retries, EOF, all arrivals and 18-turn session completion.

## Manual Pasta timeline

Customer 1 selects Pasta. The regression checks:

| Turn | Waiter | Chef | End state | Satisfaction | Total revenue |
| --- | --- | --- | --- | --- | --- |
| 1 | Seat 1 | Wait | SEATED | 92 | $0 |
| 2 | Take order 1 | Prepare 1 | PREPARING, one unit | 84 | $0 |
| 3 | Wait | Prepare 1 | READY | 76 | $0 |
| 4 | Serve 1 | Wait | SERVED, eating | 76 | $0 |
| 5 | Wait | Wait | READY_TO_PAY | 76 | $0 |
| 6 | Wait | Wait | PAID, table freed | 76 | $18 |

## Reproducible victory

Food choice is Pasta, Salad, Burger, repeating by customer ID. Both staff select the first non-Wait option when available, otherwise Wait. Each task below includes its target customer ID. The test generates the numbered CLI input from these available actions.

| Turn | Waiter | Chef |
| --- | --- | --- |
| 1 | Seat 1 | Wait |
| 2 | Take order 1 | Prepare 1 |
| 3 | Wait | Prepare 1 |
| 4 | Serve 1 | Wait |
| 5 | Seat 2 | Wait |
| 6 | Take order 2 | Prepare 2 |
| 7 | Serve 2 | Wait |
| 8 | Seat 3 | Wait |
| 9 | Take order 3 | Prepare 3 |
| 10 | Serve 3 | Wait |
| 11 | Seat 4 | Wait |
| 12 | Take order 4 | Prepare 4 |
| 13 | Seat 5 | Prepare 4 |
| 14 | Serve 4 | Wait |
| 15 | Take order 5 | Prepare 5 |
| 16 | Serve 5 | Wait |
| 17 | Seat 6 | Wait |
| 18 | Take order 6 | Prepare 6 |

Final result: Victory; 5 paid, $69.00 revenue, 74.4 average paid satisfaction, 5 served, 0 unhappy departures, 18 turns. Customer 6 has not paid.

## Reproducible defeat

On every turn from 1 through 18, select Wait for both staff (option 0 twice per turn). The generated input contains 36 zero choices.

Final result: Defeat; 0 paid, $0.00 revenue, N/A paid satisfaction, 0 served, 2 unhappy departures, 18 turns. No victory target is met. Customers 1 and 2 abandon on turns 13 and 16 respectively.

## Remaining release work

Team review, a final commit identifier, clean-checkout verification and the stage-1 release tag remain release steps. They have not been represented as completed by these local checks.