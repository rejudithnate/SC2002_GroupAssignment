package restaurantrush.entity;

/** Payment action; lifecycle scheduling and departure belong to TurnController. */
public final class Cashier {
    public void processPayment(Customer customer, Restaurant restaurant) {
        restaurant.updateRevenue(customer);
    }
}
