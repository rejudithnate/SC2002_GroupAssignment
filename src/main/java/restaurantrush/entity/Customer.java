package restaurantrush.entity;

import java.util.Objects;

public final class Customer {
    private final int customerId;
    private int satisfaction = 100;
    private CustomerStatus status = CustomerStatus.WAITING;
    private Order order;
    private Table table;

    public Customer(int customerId) {
        if (customerId < 1) { throw new IllegalArgumentException("Customer ID must be positive"); }
        this.customerId = customerId;
    }

    public int getCustomerId() { return customerId; }
    public int getSatisfaction() { return satisfaction; }
    public CustomerStatus getStatus() { return status; }
    public Order getOrder() { return order; }
    public Table getTable() { return table; }

    /** TODO: enforce the agreed customer transition rules here. */
    public void setStatus(CustomerStatus status) { this.status = Objects.requireNonNull(status); }

    /** Delegates association management to Table to keep both ends consistent. Null unseats. */
    public void setTable(Table table) {
        if (this.table == table) { return; }
        if (table == null) {
            if (this.table != null) { this.table.unAssign(); }
        } else {
            table.assign(this);
        }
    }

    void attachTable(Table table) { this.table = table; }

    public void setOrder(Order order) {
        Objects.requireNonNull(order);
        if (this.order != null) { throw new IllegalStateException("Customer already has an order"); }
        if (table == null || order.getTableId() != table.getTableId()) {
            throw new IllegalArgumentException("Order must match the customer's table");
        }
        this.order = order;
    }

    /** UML method retained, but no decay amount was supplied. Use the explicit overload. */
    public void decreaseSatisfaction() {
        throw new UnsupportedOperationException("TODO: agree the default satisfaction decay");
    }

    public void decreaseSatisfaction(int amount) {
        if (amount < 0) { throw new IllegalArgumentException("Decay cannot be negative"); }
        satisfaction = Math.max(0, satisfaction - amount);
    }
}
