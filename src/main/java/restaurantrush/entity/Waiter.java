package restaurantrush.entity;
import restaurant.model.Table;

public final class Waiter {
    
    private boolean hasActed;

    public Waiter() {
        this.hasActed = false;
    }

    public void resetForNewTurn() {
        hasActed = false;
    }

    private void requireAvailable() {
        if (hasActed) { throw new IllegalStateException("Waiter already acted this turn"); }
    }

    public void sitCustomer(Customer customer, Table table) {

        requireAvailable();

        // Check that both objects exist.
        if (customer == null || table == null) {
            throw new IllegalArgumentException(
                "Customer and table cannot be null."
            );
        }

        // Only waiting customers can be seated.
        if (customer.getStatus() != CustomerStatus.WAITING) {
            throw new IllegalStateException(
                "Customer is not waiting."
            );
        }

        // Check whether the table is empty.
        if (!table.isAvailable()) {
            throw new IllegalStateException(
                "Table is already occupied."
            );
        }

        
        table.assign(customer);
        customer.setStatus(CustomerStatus.SEATED);
        hasActed = true;
    }

   
    public void takeOrder(Customer customer) {

        requireAvailable();

        if (customer == null) {
            throw new IllegalArgumentException(
                "Customer cannot be null."
            );
        }

        if (customer.getStatus() != CustomerStatus.SEATED) {
            throw new IllegalStateException(
                "Customer must be seated before ordering."
            );
        }

        // Verify that an order has been assigned.
        // Actual order creation must be integrated separately.
        if (customer.getOrder() == null) {
            throw new IllegalStateException(
                "Customer does not have an order yet."
            );
        }

        // Update the customer to ORDERED.
        customer.setStatus(CustomerStatus.ORDERED);

        hasActed = true;
    }

    // Serves a READY order.
    public void serveOrder(Order order) {

        requireAvailable();

        if (order == null) {
            throw new IllegalArgumentException(
                "Order cannot be null."
            );
        }

        // Only READY orders can be served.
        if (order.getStatus() != OrderStatus.READY) {
            throw new IllegalStateException(
                "Order is not ready to be served."
            );
        }

        // Mark the order as SERVED.
        order.setStatus(OrderStatus.SERVED);

        hasActed = true;
    }

    // Waiter deliberately performs no task this turn.
    public void waitTurn() {

        requireAvailable();
        // Waiting also consumes the waiter's action.
        hasActed = true;
    }
}
