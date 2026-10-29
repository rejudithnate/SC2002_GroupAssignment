package restaurantrush.entity;

public final class Waiter {
    private boolean hasActed;

    public void sitCustomer(Customer customer, Table table) {
        requireAvailable();
        if (customer.getStatus() != CustomerStatus.WAITING) {
            throw new IllegalStateException("Only waiting customers can be seated");
        }
        table.assign(customer);
        customer.setStatus(CustomerStatus.SEATED);
        hasActed = true;
    }

    /** The controller must construct, register and attach the selected order first. */
    public void takeOrder(Customer customer) {
        requireAvailable();
        if (customer.getStatus() != CustomerStatus.SEATED || customer.getOrder() == null) {
            throw new IllegalStateException("A seated customer needs an attached order");
        }
        customer.setStatus(CustomerStatus.ORDERED);
        hasActed = true;
    }

    public void serveOrder(Order order) {
        requireAvailable();
        order.markServed();
        hasActed = true;
    }

    public void resetForNewTurn() { hasActed = false; }
    public boolean isAvailable() { return !hasActed; }

    public void skipTurn() {
        requireAvailable();
        hasActed = true;
    }

    private void requireAvailable() {
        if (hasActed) { throw new IllegalStateException("Waiter already acted this turn"); }
    }
}
