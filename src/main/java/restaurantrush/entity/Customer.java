
package restaurantrush.entity;

import java.util.Objects;

public final class Customer {

    private final int customerId;
    private int satisfaction = 100;
    private CustomerStatus status = CustomerStatus.WAITING;
    private Order order;
    private Table table;

    public Customer(int customerId) {
        if (customerId < 1) {
            throw new IllegalArgumentException("Customer ID must be positive");
        }
        this.customerId = customerId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public Order getOrder() {
        return order;
    }

    public Table getTable() {
        return table;
    }

    /** TODO: Enforce agreed customer status transition rules. */
    public void setStatus(CustomerStatus status) {
        this.status = Objects.requireNonNull(status);
    }


    void setTable(Table table) {
        this.table = table;
    }

    public void setOrder(Order order) {
        Objects.requireNonNull(order);

        if (this.order != null) {
            throw new IllegalStateException("Customer already has an order");
        }

        if (table == null || order.getTableId() != table.getTableId()) {
            throw new IllegalArgumentException(
                "Order must match the customer's table"
            );
        }

        this.order = order;
    }

    /** Decreases satisfaction by 8 points for regular customers. */
    public void decreaseSatisfaction() {
        decreaseSatisfaction(8);
    }

    /** Decreases satisfaction by a specified amount, minimum 0. */
    public void decreaseSatisfaction(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Decay cannot be negative");
        }

        satisfaction = Math.max(0, satisfaction - amount);
    }
}
