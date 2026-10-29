# Working together

1. Pull the latest `main` and create a short-lived branch, for example `feature/waiter-actions` or `fix/table-association`.
2. Claim a task in the team's issue tracker or chat. Use the ownership table in README to avoid simultaneous edits to the same class.
3. Agree method signatures and state transitions with dependent contributors before changing a shared contract. Contributors 1 and 2 review calls between `GameController` and `GameUI` together.
4. Keep each commit/PR focused on one behavior. Do not commit compiled output or local IDE settings.
5. Run `./scripts/build.ps1 -Test` and exercise any new console path before opening a PR. Add behavioral tests when implementing rules; update the explicit stub expectations as those methods become real.
6. Ask one teammate to review correctness and BCE boundaries, then merge. Update your branch from `main` regularly and resolve conflicts with the affected owner.

## Coding agreements

- Use Java 17-compatible code, four spaces, `UpperCamelCase` classes and `lowerCamelCase` methods/fields.
- Keep entity state private. Return immutable collection snapshots; use named methods for mutations.
- Keep console I/O in `boundary`; no `Scanner`, `System.out` or prompts in entities/controllers.
- Controllers decide when actions happen; entities validate and perform state changes. UI validation checks input syntax; controller/entity validation checks legal actions.
- Use constructor injection for boundary/control dependencies. Avoid global mutable state, singleton restaurants and unnecessary inheritance.
- Document nullability and exceptions. Absent customer/table/order references and empty queue peeks return `null` in this skeleton.
- Put rule values in one agreed configuration or policy once the game specification is available. Do not independently invent rules in different modules.
- Update `docs/ARCHITECTURE.md` when changing a UML mapping or scaffold assumption.

## PR checklist

- Describe the behavior and task completed.
- List changed public contracts and any coordination needed.
- State how the behavior was verified.
- Confirm there are no boundary imports in entities and no console I/O in controls.
- Note remaining TODOs and any deliberate UML deviation.
