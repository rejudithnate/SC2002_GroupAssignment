package restaurantrush.entity;

import java.util.Objects;

/** Owns the bidirectional table/customer association. */
public final class Table {
    private final int tableId;
    private Customer customer;

    public Table(int tableId) {
        if (tableId < 1) { throw new IllegalArgumentException("Table ID must be positive"); }
        this.tableId = tableId;
    }

    public int getTableId() { return tableId; }
    public Customer getCustomer() { return customer; }
    public boolean isAvailable() { return customer == null; }

    public void assign(Customer customer) {
        Objects.requireNonNull(customer);
        if (!isAvailable() || customer.getTable() != null) {
            throw new IllegalStateException("Table or customer already assigned");
        }
        this.customer = customer;
        customer.attachTable(this);
    }

    public void unAssign() {
        if (customer != null) {
            customer.attachTable(null);
            customer = null;
        }
    }
}
