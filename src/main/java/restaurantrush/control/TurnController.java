package restaurantrush.control;

import java.util.ArrayList;
import java.util.Objects;
import restaurantrush.entity.Customer;
import restaurantrush.entity.CustomerStatus;
import restaurantrush.entity.Order;
import restaurantrush.entity.OrderStatus;
import restaurantrush.entity.Restaurant;
import restaurantrush.entity.Table;

/** Coordinates turn phases; entities own state, the boundary owns input/output. */
public final class TurnController {
    private final Restaurant restaurant;

    public TurnController(Restaurant restaurant) {
        this.restaurant = Objects.requireNonNull(restaurant);
    }

    /**
     * Runs one turn. Round 1 uses the first eligible staff action because this
     * skeleton does not receive manager choices from GameController/GameUI.
     */
    public void runTurn(int turn) {
        if (turn < 1 || turn > 18) {
            throw new IllegalArgumentException("Turn must be between 1 and 18");
        }

        // 1. Customer arrivals.
        processArrivals(turn);

        // Reset each staff member's one-action-per-turn allowance.
        restaurant.getWaiter().resetForNewTurn();
        restaurant.getChef().resetForNewTurn();

        // Work out who finished eating BEFORE this turn's waiter service.
        // This avoids making a newly served customer pay immediately.
        processEatingAndPayment();

        // 2-3. Round 1 fallback: waiter takes the first valid action for an
        // active customer. The waiter can complete at most one action.
        for (Customer customer : new ArrayList<>(restaurant.getCustomers())) {
            if (handleWaiterAction(customer)) {
                break;
            }
        }

        // Chef acts AFTER the waiter, so an order placed this turn is eligible.
        for (Order order : new ArrayList<>(restaurant.getPendingOrders())) {
            if (handleChefAction(order)) {
                break;
            }
        }

        // 4. Cashier processes at most one previously queued customer.
        processPaidCustomer();

        // 5. Apply satisfaction loss only while waiting for service.
        updateSatisfaction();

        // 6. Remove any customers who leave unhappy.
        processUnhappyDeparture();
    }

    /**
     * In Round 1, eating lasts until the next turn after service. This method
     * runs before the waiter serves anyone in the current turn.
     */
    private void processEatingAndPayment() {
        for (Customer customer : new ArrayList<>(restaurant.getEatingCustomers())) {
            Order order = customer.getOrder();
            if (customer.getStatus() == CustomerStatus.ORDERED
                    && order != null
                    && order.getStatus() == OrderStatus.SERVED) {
                customer.setStatus(CustomerStatus.READY_TO_PAY);
                restaurant.enqueueForPayment(customer);
            }
        }
    }

    private void updateSatisfaction() {
        for (Customer customer : new ArrayList<>(restaurant.getCustomers())) {
            CustomerStatus status = customer.getStatus();
            Order order = customer.getOrder();

            if (status == CustomerStatus.WAITING
                    || status == CustomerStatus.SEATED
                    || (status == CustomerStatus.ORDERED
                        && (order == null || order.getStatus() != OrderStatus.SERVED))) {
                // Customer.decreaseSatisfaction() applies the agreed 8-point
                // decay and prevents satisfaction falling below zero.
                customer.decreaseSatisfaction();
            }
        }
    }

    private void processUnhappyDeparture() {
        for (Customer customer : new ArrayList<>(restaurant.getCustomers())) {
            CustomerStatus status = customer.getStatus();
            Order order = customer.getOrder();
            boolean waitingForService = status == CustomerStatus.WAITING
                    || status == CustomerStatus.SEATED
                    || (status == CustomerStatus.ORDERED
                        && (order == null || order.getStatus() != OrderStatus.SERVED));

            if (!waitingForService || customer.getSatisfaction() > 0) {
                continue;
            }

            // Cancel an unfinished order, if the customer has one.
            if (order != null && order.getStatus() != OrderStatus.CANCELLED
                    && order.getStatus() != OrderStatus.PAID
                    && order.getStatus() != OrderStatus.SERVED) {
                order.cancel();
            }

            // Free their table, if they had been seated.
            Table table = customer.getTable();
            if (table != null && table.getCustomer() == customer) {
                table.unAssign();
            }

            customer.setStatus(CustomerStatus.CANCELLED);
            restaurant.setUnhappyDepartures(restaurant.getUnhappyDepartures() + 1);
            restaurant.removeCustomer(customer);
        }
    }

    private void processPaidCustomer() {
        // getNextPayingCustomer() must REMOVE the next customer from the queue.
        Customer customer = restaurant.getNextPayingCustomer();
        if (customer == null) {
            return;
        }
        if (customer.getStatus() != CustomerStatus.READY_TO_PAY) {
            throw new IllegalStateException("Payment queue contains a customer who is not ready to pay");
        }

        // Cashier handles payment and revenue; do not update revenue twice.
        restaurant.getCashier().processPayment(customer, restaurant);

        Order order = customer.getOrder();
        if (order != null && order.getStatus() == OrderStatus.SERVED) {
            order.markPaid();
        }
        customer.setStatus(CustomerStatus.PAID);

        // Clean up the now-completed customer.
        Table table = customer.getTable();
        if (table != null && table.getCustomer() == customer) {
            table.unAssign();
        }
        restaurant.removeCustomer(customer);
    }

    private void processArrivals(int turn) {
        // One customer arrives on turns 1, 4, 7, 10, 13 and 16.
        if (turn <= 16 && (turn - 1) % 3 == 0) {
            int customerId = ((turn - 1) / 3) + 1;
            restaurant.addWaitingCustomer(new Customer(customerId));
        }
    }

    private boolean handleChefAction(Order order) {
        if (order == null || !restaurant.getChef().isAvailable()
                || order.getStatus() != OrderStatus.PREPARING) {
            return false;
        }
        restaurant.getChef().prepareOrder(order);
        return true;
    }

    private boolean handleWaiterAction(Customer customer) {
        if (customer == null || !restaurant.getWaiter().isAvailable()) {
            return false;
        }

        if (customer.getStatus() == CustomerStatus.WAITING) {
            Table table = restaurant.getAvailTable();
            if (table == null) {
                return false;
            }
            restaurant.getWaiter().sitCustomer(customer, table);
            return true;
        }

        if (customer.getStatus() == CustomerStatus.SEATED) {
            restaurant.getWaiter().takeOrder(customer);
            return true;
        }

        if (customer.getStatus() == CustomerStatus.ORDERED) {
            Order order = customer.getOrder();
            if (order != null && order.getStatus() == OrderStatus.READY) {
                restaurant.getWaiter().serveOrder(order);
                return true;
            }
        }

        // No valid action for this customer: try the next one.
        return false;
    }
}
