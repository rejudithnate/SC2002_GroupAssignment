package restaurantrush.entity;

import java.util.Objects;

public final class Cashier {
    public void processPayment(Customer customer, Restaurant restaurant) {
        Objects.requireNonNull(restaurant).completePayment(customer);
    }
}
