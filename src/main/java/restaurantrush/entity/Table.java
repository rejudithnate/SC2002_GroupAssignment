package restaurantrush.entity;
import java.util.Objects;

/** Owns the bidirectional table/customer association. */
public final class Table {

    // Keyword final used to prevent variable from being reassigned
    private final int tableId; 

    // Customer currently occupying this table. null means the table is empty.
    private Customer customer;

    public Table(int tableId) {
        this.tableId = tableId;
        this.customer = null;
    }

    public int getTableId() {
        return tableId;
    }

    public Customer getCustomer() {
        return customer;
    }

    // Returns true if no customer
    public boolean isAvailable() {
        return customer == null;
    }

    public void assign(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }
        if (!isAvailable()) {
            throw new IllegalStateException("Table is already occupied.");
        }
        if (customer.getTable() != null) {
            throw new IllegalStateException("Customer already has a table.");
        }

        this.customer = customer;
        customer.setTable(this);
    }

    public void unAssign() {
        if (customer != null) {
            Customer previousCustomer = customer;
            this.customer = null;

            // Set previous customer's table to nothing
            previousCustomer.setTable(null);
        }
    }
}

