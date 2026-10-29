package restaurantrush.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import restaurantrush.boundary.GameUI;
import restaurantrush.entity.Customer;
import restaurantrush.entity.CustomerStatus;
import restaurantrush.entity.Order;
import restaurantrush.entity.OrderStatus;
import restaurantrush.entity.Restaurant;

/** Coordinates phases and validates manager choices before either staff member acts. */
public final class TurnController {
    public enum Task { WAIT, SEAT, TAKE_ORDER, SERVE, PREPARE }

    /** Target is a customer ID; WAIT uses zero. */
    public record Action(Task task, int customerId) {
        public Action {
            Objects.requireNonNull(task);
            if (task == Task.WAIT ? customerId != 0 : customerId < 1) {
                throw new IllegalArgumentException("An action needs a valid customer ID");
            }
        }
        public static Action waitAction() { return new Action(Task.WAIT, 0); }
    }

    private final Restaurant restaurant;
    private int completedTurns;
    private int activeTurn;

    public TurnController(Restaurant restaurant) {
        this.restaurant = Objects.requireNonNull(restaurant);
    }

    public Restaurant getRestaurant() { return restaurant; }
    public int getCompletedTurns() { return completedTurns; }

    /** Starts the next sequential turn; cannot reset staff by starting a turn twice. */
    public void beginTurn(int turn) {
        if (activeTurn != 0 || turn != completedTurns + 1 || turn > GameController.MAX_TURN) {
            throw new IllegalStateException("Turns must run once, in order, from 1 to 18");
        }
        if ((turn - 1) % 3 == 0) {
            restaurant.addWaitingCustomer(new Customer((turn - 1) / 3 + 1));
        }
        restaurant.getWaiter().resetForNewTurn();
        restaurant.getChef().resetForNewTurn();
        activeTurn = turn;
    }

    public List<Action> getWaiterActions() {
        requireActiveTurn();
        List<Action> actions = new ArrayList<>();
        actions.add(Action.waitAction());
        for (Customer customer : restaurant.getCustomers()) {
            int id = customer.getCustomerId();
            if (customer.getStatus() == CustomerStatus.WAITING && restaurant.getAvailableTable() != null) {
                actions.add(new Action(Task.SEAT, id));
            } else if (customer.getStatus() == CustomerStatus.SEATED) {
                actions.add(new Action(Task.TAKE_ORDER, id));
            } else if (customer.getStatus() == CustomerStatus.ORDERED
                    && customer.getOrder().getStatus() == OrderStatus.READY) {
                actions.add(new Action(Task.SERVE, id));
            }
        }
        return List.copyOf(actions);
    }

    /** Includes the order the selected waiter action will create this turn. */
    public List<Action> getChefActions(Action waiterAction) {
        if (!getWaiterActions().contains(waiterAction)) {
            throw new IllegalArgumentException("That waiter action is not available");
        }
        List<Action> actions = new ArrayList<>();
        actions.add(Action.waitAction());
        for (Customer customer : restaurant.getCustomers()) {
            Order order = customer.getOrder();
            if ((order != null && order.getStatus() == OrderStatus.PREPARING)
                    || (waiterAction.task() == Task.TAKE_ORDER
                        && waiterAction.customerId() == customer.getCustomerId())) {
                actions.add(new Action(Task.PREPARE, customer.getCustomerId()));
            }
        }
        return List.copyOf(actions);
    }

    public void completeTurn(Action waiterAction, Action chefAction) {
        requireActiveTurn();
        if (!getChefActions(waiterAction).contains(chefAction)) {
            throw new IllegalArgumentException("That chef action is not available");
        }
        // Both selections are legal before any action is consumed.
        switch (waiterAction.task()) {
            case WAIT -> restaurant.getWaiter().waitTurn();
            case SEAT -> restaurant.seatCustomer(waiterAction.customerId());
            case TAKE_ORDER -> restaurant.takeOrder(waiterAction.customerId());
            case SERVE -> restaurant.serveCustomer(waiterAction.customerId(), activeTurn);
            default -> throw new IllegalArgumentException("Invalid waiter task");
        }
        if (chefAction.task() == Task.WAIT) {
            restaurant.getChef().waitTurn();
        } else {
            restaurant.getChef().prepareOrder(restaurant.getCustomer(chefAction.customerId()).getOrder());
        }

        restaurant.finishEating(activeTurn);
        Customer paying = restaurant.getEligiblePayingCustomer(activeTurn);
        if (paying != null) { restaurant.getCashier().processPayment(paying, restaurant); }
        restaurant.applyWaitingAndDepartures();
        completedTurns = activeTurn;
        activeTurn = 0;
    }

    /** Returns false when console input ends, without declaring an unfinished game a defeat. */
    public boolean runTurn(int turn, GameUI ui) {
        Objects.requireNonNull(ui);
        beginTurn(turn);
        ui.displayGameState(restaurant, turn, GameController.MAX_TURN);
        Optional<Action> waiterAction = ui.readAction("Waiter", getWaiterActions());
        if (waiterAction.isEmpty()) { return false; }
        Optional<Action> chefAction = ui.readAction("Chef", getChefActions(waiterAction.get()));
        if (chefAction.isEmpty()) { return false; }
        completeTurn(waiterAction.get(), chefAction.get());
        if (waiterAction.get().task() == Task.TAKE_ORDER) {
            ui.displayFoodSelection(restaurant.getCustomer(waiterAction.get().customerId()));
        }
        ui.displayTurnSummary(restaurant, turn, GameController.MAX_TURN);
        return true;
    }

    private void requireActiveTurn() {
        if (activeTurn == 0) { throw new IllegalStateException("Start a turn before choosing actions"); }
    }
}