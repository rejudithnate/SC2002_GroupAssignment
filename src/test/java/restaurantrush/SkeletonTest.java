package restaurantrush;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;
import restaurantrush.boundary.GameUI;
import restaurantrush.control.GameController;
import restaurantrush.control.TurnController;
import restaurantrush.entity.*;

/** Dependency-free contract checks. Run through scripts/build.ps1 -Test. */
public final class SkeletonTest {
    private SkeletonTest() { }

    public static void main(String[] args) {
        tableAssociation();
        orderAndPaymentLifecycle();
        queuesAndCollections();
        boundaryInput();
        controllerDisplay();
        System.out.println("Passed 5 skeleton contract checks.");
    }

    private static void tableAssociation() {
        Customer customer = new Customer(1);
        Table table = new Table(1);
        table.assign(customer);
        check(customer.getTable() == table && table.getCustomer() == customer, "Association must be bidirectional");
        expect(IllegalStateException.class, () -> new Table(2).assign(customer));
        customer.setTable(null);
        check(table.isAvailable() && customer.getTable() == null, "Unseating must clear both ends");
        customer.decreaseSatisfaction(200);
        check(customer.getSatisfaction() == 0, "Satisfaction must not become negative");
    }

    private static void orderAndPaymentLifecycle() {
        Restaurant restaurant = new Restaurant();
        Customer customer = new Customer(1);
        restaurant.addWaitingCustomer(customer);
        Waiter waiter = restaurant.getWaiter();
        waiter.sitCustomer(customer, restaurant.getAvailableTable());
        restaurant.pollWaitingCustomer();
        expect(IllegalStateException.class, waiter::skipTurn);
        Order order = new Order(1, List.of(new MenuItem("Test dish", 12.5, 2)));
        restaurant.addOrder(order);
        customer.setOrder(order);
        waiter.resetForNewTurn();
        waiter.takeOrder(customer);
        Chef chef = restaurant.getChef();
        chef.prepareOrder(order);
        expect(IllegalStateException.class, () -> chef.prepareOrder(order));
        chef.resetForNewTurn();
        chef.prepareOrder(order);
        check(order.getStatus() == OrderStatus.READY, "Order should become ready");
        waiter.resetForNewTurn();
        waiter.serveOrder(order);
        customer.setStatus(CustomerStatus.READY_TO_PAY);
        restaurant.enqueueForPayment(customer);
        restaurant.getCashier().processPayment(restaurant.getNextPayingCustomer(), restaurant);
        expect(IllegalStateException.class, () -> restaurant.getCashier().processPayment(customer, restaurant));
        check(restaurant.getRevenue() == 12.5 && restaurant.getServedCustomers() == 1, "Payment must count once");
        check(customer.getStatus() == CustomerStatus.PAID && order.getStatus() == OrderStatus.PAID, "Payment state must agree");
        restaurant.removePayingCustomer(customer);
        customer.setTable(null);
        restaurant.removeClosedOrder(order);
        check(restaurant.getOrders().isEmpty() && restaurant.getNextPayingCustomer() == null, "Cleanup must release order and queue");
    }

    private static void queuesAndCollections() {
        Restaurant restaurant = new Restaurant();
        Customer first = new Customer(1);
        Customer second = new Customer(2);
        restaurant.addWaitingCustomer(first);
        restaurant.addWaitingCustomer(second);
        expect(IllegalArgumentException.class, () -> restaurant.addWaitingCustomer(new Customer(1)));
        expect(UnsupportedOperationException.class, () -> restaurant.getWaitingCustomers().clear());
        check(restaurant.pollWaitingCustomer() == first && restaurant.pollWaitingCustomer() == second, "Queue must be FIFO");
        check(restaurant.getTables().size() == 2, "UML specifies two tables");
        for (int index = 0; index < Restaurant.MAX_ORDERS; index++) {
            restaurant.addOrder(new Order(1, List.of(new MenuItem("Test", 1, 1))));
        }
        expect(IllegalStateException.class, () -> restaurant.addOrder(new Order(1, List.of(new MenuItem("Test", 1, 1)))));
    }

    private static void boundaryInput() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Scanner input = new Scanner("bad\n2\n"); PrintStream stream = new PrintStream(output)) {
            GameUI ui = new GameUI(input, stream);
            check(ui.getWaiterChoice() == 2, "Boundary must retry invalid integer input");
            expect(IllegalStateException.class, ui::readChefChoice);
        }
    }

    private static void controllerDisplay() {
        Restaurant restaurant = new Restaurant();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Scanner input = new Scanner(""); PrintStream stream = new PrintStream(output)) {
            GameUI ui = new GameUI(input, stream);
            GameController game = new GameController(restaurant, new TurnController(restaurant), ui);
            game.startGame();
            check(output.toString().contains("Turn 1/18"), "Controller must display the initial turn through GameUI");
            output.reset();
            restaurant.addWaitingCustomer(new Customer(1));
            game.setCurrentTurn(4);
            game.startGame();
            check(output.toString().contains("Turn 4/18") && output.toString().contains("Waiting: 1"),
                    "Display must read the current turn and restaurant state");
            expect(IllegalArgumentException.class, () -> game.setCurrentTurn(19));
            // Replace these expectations when implementing the corresponding gameplay tasks.
            expect(UnsupportedOperationException.class, game::isVictory);
            expect(UnsupportedOperationException.class, game::startTurn);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }

    private static void expect(Class<? extends Throwable> expected, Runnable action) {
        try {
            action.run();
        } catch (Throwable exception) {
            if (expected.isInstance(exception)) { return; }
            throw new AssertionError("Expected " + expected.getSimpleName(), exception);
        }
        throw new AssertionError("Expected " + expected.getSimpleName());
    }

}
