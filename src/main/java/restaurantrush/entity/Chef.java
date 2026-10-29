package restaurantrush.entity;

import java.util.Objects;

public final class Chef {
    private boolean hasActed;

    public boolean isAvailable() { return !hasActed; }
    public void resetForNewTurn() { hasActed = false; }

    private void requireAvailable() {
        if (hasActed) { throw new IllegalStateException("Chef already acted this turn"); }
    }

    public void prepareOrder(Order order) {
        requireAvailable();
        Objects.requireNonNull(order).prepareUnit();
        hasActed = true;
    }

    public void waitTurn() {
        requireAvailable();
        hasActed = true;
    }
}
