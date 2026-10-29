package restaurantrush.entity;

public final class Cashier {

    public void processPayment(
            Customer customer,
            Restaurant restaurant) {

        if (customer == null || restaurant == null) {
            throw new IllegalArgumentException(
                "Customer and restaurant are required."
            );
        }

        if (customer.getStatus()
                != CustomerStatus.READY_TO_PAY) {
            throw new IllegalStateException(
                "Customer is not ready to pay."
            );
        }

        Order order = customer.getOrder();

        if (order == null
                || order.getStatus() != OrderStatus.SERVED) {
            throw new IllegalStateException(
                "No served order available for payment."
            );
        }

        Table table = customer.getTable();

        if (table == null || table.getCustomer() != customer) {
            throw new IllegalStateException(
                "Customer does not occupy a valid table."
            );
        }

        restaurant.updateRevenue(customer);

        order.setStatus(OrderStatus.PAID);
        customer.setStatus(CustomerStatus.PAID);

        table.unAssign();
    }
}

