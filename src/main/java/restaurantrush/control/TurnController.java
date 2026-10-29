package restaurantrush.control;

import java.util.Objects;
import restaurantrush.entity.Customer;
import restaurantrush.entity.Order;
import restaurantrush.entity.Restaurant;

/** Coordinates turn phases; entities own state, the boundary owns input/output. */
public final class TurnController {
    private final Restaurant restaurant;

    public TurnController(Restaurant restaurant) {
        this.restaurant = Objects.requireNonNull(restaurant);
    }

    /** TODO: connect the phases below in the order required by the game specification. */
    public void runTurn(int turn) {
        if (turn < 1) { throw new IllegalArgumentException("Turn must be positive"); }
        // Fail explicitly until this is implemented: do not simulate a successful empty turn.
        throw new UnsupportedOperationException("TODO: implement turn sequencing and action selection");
    }

    private void processEatingAndPayment() {
        // TODO: decide eating duration, then queue eligible customers for payment.
    }

    private void updateSatisfaction() {
        // TODO: apply the agreed decay for each customer status.
    }

    private void processUnhappyDeparture() {
        // TODO: cancel orders, free tables and remove customers at the agreed threshold.
    }

    private void processPaidCustomer() {
        // TODO: use Cashier, free tables and record completed customers exactly once.
    }

    private void processArrivals(int turn) {
        // TODO: add customers according to the agreed arrival schedule.
    }

    private boolean handleChefAction(Order order) {
        if (!restaurant.getChef().isAvailable()) { return false; }
        restaurant.getChef().prepareOrder(order);
        return true;
    }

    private boolean handleWaiterAction(Customer customer) {
        // TODO: dispatch seat / take order / serve / skip using a validated action choice.
        throw new UnsupportedOperationException("TODO: implement waiter action dispatch");
    }
}
