package restaurant.staff;

import restaurant.model.Order;

public class Chef {
    private boolean hasActed;

    public Chef() {
        hasActed = false;
    }

    private void requireAvailable() {
        if (hasActed) { throw new IllegalStateException("Chef already acted this turn"); }
    }

    public void resetForNewTurn() {
        hasActed = false;
    }

    public void prepareOrder(Order order) {
        requireAvailable();

        if (order == null) {
            throw new IllegalArgumentException(
                "Order is required."
            );
        }

        order.prepareUnit();
        hasActed = true;
    }

    public void waitTurn() {
        requireAvailable();
        hasActed = true;
    }
}
