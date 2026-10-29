package restaurantrush.entity;

import java.util.Objects;

public final class Waiter {
    private boolean hasActed;

    public boolean isAvailable() { return !hasActed; }
    public void resetForNewTurn() { hasActed = false; }

    private void requireAvailable() {
        if (hasActed) { throw new IllegalStateException("Waiter already acted this turn"); }
    }

    public void sitCustomer(Customer customer, Table table) {
        requireAvailable();
        Objects.requireNonNull(customer);
        Objects.requireNonNull(table);
        if (customer.getStatus() != CustomerStatus.WAITING) {
            throw new IllegalStateException("Customer must be waiting");
        }
        table.assign(customer);
        customer.setStatus(CustomerStatus.SEATED);
        hasActed = true;
    }

    public void takeOrder(Customer customer) {
        requireAvailable();
        Objects.requireNonNull(customer);
        if (customer.getStatus() != CustomerStatus.SEATED || customer.getOrder() == null
                || customer.getOrder().getStatus() != OrderStatus.PREPARING) {
            throw new IllegalStateException("Customer must be seated with a new order");
        }
        customer.setStatus(CustomerStatus.ORDERED);
        hasActed = true;
    }

    public void serveOrder(Order order) {
        requireAvailable();
        Objects.requireNonNull(order).markServed();
        hasActed = true;
    }

    public void waitTurn() {
        requireAvailable();
        hasActed = true;
    }
}
