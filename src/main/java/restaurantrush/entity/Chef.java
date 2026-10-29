package restaurantrush.entity;

public final class Chef {
    private boolean hasActed;

    public Chef() { }

    public void prepareOrder(Order order) {
        requireAvailable();
        order.prepareUnit();
        hasActed = true;
    }

    public boolean isAvailable() { return !hasActed; }
    public void resetForNewTurn() { hasActed = false; }

    /** Called skipTurn because Object.wait() is final in Java. */
    public void skipTurn() {
        requireAvailable();
        hasActed = true;
    }

    private void requireAvailable() {
        if (hasActed) { throw new IllegalStateException("Chef already acted this turn"); }
    }
}
