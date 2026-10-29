package restaurantrush;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import restaurantrush.boundary.GameUI;
import restaurantrush.control.GameController;
import restaurantrush.control.TurnController;
import restaurantrush.control.TurnController.Action;
import restaurantrush.control.TurnController.Task;
import restaurantrush.entity.*;

/** Offline behavioral regression checks, including real console-session replays. */
public final class SkeletonTest {
    private static int checks;
    private SkeletonTest() { }

    public static void main(String[] args) throws Exception {
        menuAndMoney();
        tableAssociation();
        staffLimits();
        pastaTimeline();
        invalidActions();
        selectedSeatingAndFullTables();
        abandonment();
        paymentQueueAndHistory();
        onePaymentPerTurn();
        victoryBoundaries();
        boundaryInput();
        fullSessions();
        System.out.println("Passed " + checks + " behavioral assertions.");
    }

    private static Action action(Task task, int customer) { return new Action(task, customer); }
    private static Action waitAction() { return Action.waitAction(); }

    private static void play(TurnController turns, int turn, Action waiter, Action chef) {
        turns.beginTurn(turn);
        turns.completeTurn(waiter, chef);
    }

    private static void menuAndMoney() {
        Menu menu = new Menu();
        check(menu.getMenuItems().size() == 3, "Three menu items");
        check(menu.getMenuItems().get(0).getPriceCents() == 900, "Salad costs $9");
        check(menu.getMenuItems().get(1).getPriceCents() == 1500, "Burger costs $15");
        check(menu.chooseForCustomer(1).getName().equals("Pasta"), "Customer one chooses Pasta");
        check(menu.chooseForCustomer(4) == menu.chooseForCustomer(1), "Selection repeats deterministically");
        MenuItem fractional = new MenuItem("Test", 12.50, 1);
        check(fractional.getPriceCents() == 1250, "Fractional prices retain cents");
        expect(IllegalArgumentException.class, () -> new MenuItem("Test", 1.001, 1));
        expect(IllegalArgumentException.class, () -> new MenuItem("Test", Double.NaN, 1));
        Order order = new Order(1, List.of(menu.chooseForCustomer(1)));
        expect(IllegalStateException.class, order::markServed);
        order.prepareUnit();
        check(order.getStatus() == OrderStatus.PREPARING, "Pasta requires two units");
        order.prepareUnit();
        check(order.getStatus() == OrderStatus.READY, "Pasta ready after two units");
        expect(IllegalStateException.class, order::prepareUnit);
        order.markServed();
        expect(IllegalStateException.class, order::cancel);
        order.markPaid();
        expect(IllegalStateException.class, order::markPaid);
        expect(UnsupportedOperationException.class, () -> menu.getMenuItems().clear());
    }

    private static void tableAssociation() {
        Customer customer = new Customer(1);
        Table table = new Table(1);
        table.assign(customer);
        check(customer.getTable() == table && table.getCustomer() == customer, "Bidirectional association");
        expect(IllegalStateException.class, () -> new Table(2).assign(customer));
        table.unAssign();
        check(customer.getTable() == null && table.isAvailable(), "Unseating clears both ends");
        expect(IllegalArgumentException.class, () -> new Table(0));
        customer.decreaseSatisfaction(200);
        check(customer.getSatisfaction() == 0, "Satisfaction floors at zero");
        expect(IllegalArgumentException.class, () -> customer.decreaseSatisfaction(-1));
    }

    private static void staffLimits() {
        Waiter waiter = new Waiter();
        Customer customer = new Customer(1);
        expect(IllegalStateException.class, () -> waiter.takeOrder(customer));
        check(waiter.isAvailable(), "Invalid action does not consume waiter");
        waiter.sitCustomer(customer, new Table(1));
        expect(IllegalStateException.class, waiter::waitTurn);
        waiter.resetForNewTurn();
        waiter.waitTurn();
        check(!waiter.isAvailable(), "Wait consumes waiter action");
        Chef chef = new Chef();
        Order order = new Order(1, List.of(new Menu().chooseForCustomer(2)));
        chef.prepareOrder(order);
        expect(IllegalStateException.class, () -> chef.prepareOrder(order));
        chef.resetForNewTurn();
        expect(IllegalStateException.class, () -> chef.prepareOrder(order));
        check(chef.isAvailable(), "Rejected preparation preserves action");
        chef.waitTurn();
        expect(IllegalStateException.class, chef::waitTurn);
    }

    private static void pastaTimeline() {
        Restaurant restaurant = new Restaurant();
        TurnController turns = new TurnController(restaurant);
        check(restaurant.getPaidSatisfactionAverage().isEmpty(), "No paid average before payment");
        play(turns, 1, action(Task.SEAT, 1), waitAction());
        Customer customer = restaurant.getCustomer(1);
        check(customer.getSatisfaction() == 92, "Pasta turn 1: 92");
        play(turns, 2, action(Task.TAKE_ORDER, 1), action(Task.PREPARE, 1));
        check(customer.getSatisfaction() == 84 && customer.getOrder().getCookedUnit() == 1,
                "Pasta turn 2: order and first unit, 84");
        play(turns, 3, waitAction(), action(Task.PREPARE, 1));
        check(customer.getSatisfaction() == 76 && customer.getOrder().getStatus() == OrderStatus.READY,
                "Pasta turn 3: ready, 76");
        play(turns, 4, action(Task.SERVE, 1), waitAction());
        check(customer.getStatus() == CustomerStatus.ORDERED && restaurant.getServedCustomers() == 1,
                "Pasta turn 4: eating, served count increments");
        check(restaurant.getPaidCustomerCount() == 0, "Serving is not payment");
        play(turns, 5, waitAction(), waitAction());
        check(customer.getStatus() == CustomerStatus.READY_TO_PAY && restaurant.getRevenueCents() == 0,
                "Pasta turn 5: ready but cannot pay");
        check(customer.getTable() != null, "Table remains occupied until payment");
        play(turns, 6, waitAction(), waitAction());
        check(customer.getStatus() == CustomerStatus.PAID && restaurant.getRevenueCents() == 1800,
                "Pasta turn 6: paid $18");
        check(customer.getSatisfaction() == 76 && restaurant.getAvgSatisfaction() == 76,
                "Paid satisfaction is 76");
        check(customer.getTable() == null && restaurant.getNextPayingCustomer() == null
                && restaurant.getOrders().isEmpty(), "Payment cleans table, queue and active order");
        expect(IllegalStateException.class, () -> restaurant.getCashier().processPayment(customer, restaurant));
        check(restaurant.getPaidCustomerCount() == 1, "Duplicate payment cannot change count");
        expect(IllegalStateException.class, () -> turns.beginTurn(6));
        expect(IllegalStateException.class, () -> turns.beginTurn(8));
    }

    private static void invalidActions() {
        Restaurant restaurant = new Restaurant();
        TurnController turns = new TurnController(restaurant);
        turns.beginTurn(1);
        expect(IllegalStateException.class, () -> turns.beginTurn(1));
        expect(IllegalArgumentException.class,
                () -> turns.completeTurn(action(Task.SERVE, 1), waitAction()));
        expect(IllegalArgumentException.class,
                () -> turns.completeTurn(action(Task.SEAT, 1), action(Task.PREPARE, 1)));
        check(restaurant.getCustomer(1).getStatus() == CustomerStatus.WAITING
                && restaurant.getWaiter().isAvailable() && restaurant.getChef().isAvailable(),
                "Both choices validated before either action mutates state");
        turns.completeTurn(action(Task.SEAT, 1), waitAction());
        turns.beginTurn(2);
        expect(IllegalArgumentException.class,
                () -> turns.completeTurn(action(Task.TAKE_ORDER, 1), action(Task.PREPARE, 99)));
        check(restaurant.getCustomer(1).getOrder() == null, "Rejected plan does not attach an order");
        turns.completeTurn(action(Task.TAKE_ORDER, 1), action(Task.PREPARE, 1));
        turns.beginTurn(3);
        check(!turns.getWaiterActions().contains(action(Task.SERVE, 1)), "Cannot serve before Chef completes dish");
        turns.completeTurn(waitAction(), action(Task.PREPARE, 1));
        expect(IllegalStateException.class, () -> turns.completeTurn(waitAction(), waitAction()));
    }

    private static void selectedSeatingAndFullTables() {
        Restaurant restaurant = new Restaurant();
        for (int id = 1; id <= 3; id++) { restaurant.addWaitingCustomer(new Customer(id)); }
        restaurant.seatCustomer(2);
        check(restaurant.getWaitingCustomers().stream().map(Customer::getCustomerId).toList().equals(List.of(1, 3)),
                "Manager can seat a selected customer instead of queue head");
        restaurant.getWaiter().resetForNewTurn();
        restaurant.seatCustomer(1);
        restaurant.getWaiter().resetForNewTurn();
        expect(IllegalStateException.class, () -> restaurant.seatCustomer(3));
        check(restaurant.getWaiter().isAvailable() && restaurant.getCustomer(3).getTable() == null,
                "Full-table failure leaves customer and staff unchanged");
        expect(IllegalArgumentException.class, () -> restaurant.addWaitingCustomer(new Customer(1)));
        expect(UnsupportedOperationException.class, () -> restaurant.getWaitingCustomers().clear());
        expect(UnsupportedOperationException.class, () -> restaurant.getCustomers().clear());
        expect(UnsupportedOperationException.class, () -> restaurant.getOrders().clear());
    }

    private static void abandonment() {
        Restaurant restaurant = new Restaurant();
        restaurant.addWaitingCustomer(new Customer(1));
        restaurant.seatCustomer(1);
        restaurant.getWaiter().resetForNewTurn();
        Order order = restaurant.takeOrder(1);
        for (int turn = 1; turn <= 13; turn++) { restaurant.applyWaitingAndDepartures(); }
        check(order.getStatus() == OrderStatus.CANCELLED, "Abandonment cancels unfinished order");
        check(restaurant.getCustomer(1).getStatus() == CustomerStatus.CANCELLED
                && restaurant.getCustomer(1).getTable() == null && restaurant.getCustomers().isEmpty(),
                "Abandonment removes customer and frees table");
        check(restaurant.getUnhappyCustomers() == 1 && restaurant.getRevenueCents() == 0
                && restaurant.getOrders().isEmpty(), "Abandonment counts once without revenue");
        restaurant.applyWaitingAndDepartures();
        check(restaurant.getUnhappyCustomers() == 1, "Departure cannot count twice");
        expect(IllegalArgumentException.class, () -> restaurant.addWaitingCustomer(new Customer(1)));
    }

    /** Domain fixture with two customers ready together, to stress FIFO and one-payment limits. */
    private static Restaurant readyPair() {
        Restaurant restaurant = new Restaurant();
        for (int id : List.of(5, 6)) {
            restaurant.addWaitingCustomer(new Customer(id));
            restaurant.getWaiter().resetForNewTurn();
            restaurant.seatCustomer(id);
            restaurant.getWaiter().resetForNewTurn();
            Order order = restaurant.takeOrder(id);
            restaurant.getChef().resetForNewTurn();
            restaurant.getChef().prepareOrder(order);
            restaurant.getWaiter().resetForNewTurn();
            restaurant.serveCustomer(id, 1);
        }
        restaurant.getCustomer(5).decreaseSatisfaction(40);
        restaurant.getCustomer(6).decreaseSatisfaction(20);
        restaurant.finishEating(2);
        return restaurant;
    }

    private static void paymentQueueAndHistory() {
        Restaurant restaurant = readyPair();
        Customer first = restaurant.getCustomer(5);
        Customer second = restaurant.getCustomer(6);
        expect(IllegalStateException.class, () -> restaurant.completePayment(second));
        check(restaurant.getRevenueCents() == 0 && restaurant.getNextPayingCustomer() == first,
                "Out-of-order payment is rejected without changes");
        check(restaurant.getEligiblePayingCustomer(2) == null, "Newly ready customers cannot pay");
        restaurant.getCashier().processPayment(restaurant.getEligiblePayingCustomer(3), restaurant);
        check(restaurant.getNextPayingCustomer() == second && restaurant.getAvgSatisfaction() == 60,
                "FIFO payment records only paid satisfaction");
        restaurant.getCashier().processPayment(second, restaurant);
        first.decreaseSatisfaction(60);
        check(restaurant.getAvgSatisfaction() == 70 && restaurant.getPaidCustomerCount() == 2,
                "Average uses snapshots at payment, not mutable customer values");
        check(restaurant.getRevenueCents() == 2400 && restaurant.getCustomers().isEmpty(),
                "Both bills recorded and paid customers leave active population");
    }

    private static void onePaymentPerTurn() {
        Restaurant restaurant = readyPair();
        TurnController turns = new TurnController(restaurant);
        play(turns, 1, waitAction(), waitAction());
        play(turns, 2, waitAction(), waitAction());
        play(turns, 3, waitAction(), waitAction());
        check(restaurant.getPaidCustomerCount() == 1 && restaurant.getNextPayingCustomer().getCustomerId() == 6,
                "Cashier processes at most one eligible customer per turn");
        play(turns, 4, waitAction(), waitAction());
        check(restaurant.getPaidCustomerCount() == 2, "Next oldest customer pays next turn");
    }

    private static void victoryBoundaries() {
        check(GameController.meetsVictoryTargets(4, 4500, 60), "Exact targets win");
        check(!GameController.meetsVictoryTargets(3, 4500, 60), "Paid-count target required");
        check(!GameController.meetsVictoryTargets(4, 4499, 60), "Revenue target required");
        check(!GameController.meetsVictoryTargets(4, 4500, 59.99), "Average target required");
        check(!GameController.meetsVictoryTargets(4, 4500, Double.NaN), "Undefined average cannot win");
    }

    private static void boundaryInput() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Scanner input = new Scanner("bad\n99\n1\n"); PrintStream stream = new PrintStream(output)) {
            GameUI ui = new GameUI(input, stream);
            List<Action> choices = List.of(waitAction(), action(Task.SEAT, 1));
            check(ui.readAction("Waiter", choices).orElseThrow().equals(action(Task.SEAT, 1)),
                    "UI retries malformed and unavailable input");
            check(ui.readAction("Chef", List.of(waitAction())).isEmpty(), "EOF returns without throwing");
            ui.displayGameState(new Restaurant(), 1, 18);
        }
        String text = output.toString(StandardCharsets.UTF_8);
        check(text.contains("whole number") && text.contains("unavailable"), "UI explains rejections");
        check(text.contains("$0.00") && text.contains("N/A") && text.contains("Table 2: Available"),
                "UI formats money, empty paid average and tables");

        for (String inputText : List.of("", "1\n")) {
            Restaurant restaurant = new Restaurant();
            output.reset();
            try (Scanner input = new Scanner(inputText); PrintStream stream = new PrintStream(output)) {
                GameController game = new GameController(restaurant, new TurnController(restaurant), new GameUI(input, stream));
                game.startGame();
                check(game.getCurrentTurn() == 0 && !game.isVictory(), "EOF cannot complete a turn or win");
                check(restaurant.getCustomer(1).getStatus() == CustomerStatus.WAITING,
                        "EOF before both selections does not execute waiter action");
                expect(IllegalStateException.class, game::startGame);
            }
            check(output.toString(StandardCharsets.UTF_8).contains("Input ended.")
                    && !output.toString(StandardCharsets.UTF_8).contains("Defeat."),
                    "EOF exits gracefully without a false defeat");
        }
    }

    private static void fullSessions() throws Exception {
        Restaurant simulation = new Restaurant();
        TurnController turns = new TurnController(simulation);
        StringBuilder input = new StringBuilder();
        List<String> trace = new ArrayList<>();
        for (int turn = 1; turn <= GameController.MAX_TURN; turn++) {
            turns.beginTurn(turn);
            List<Action> waiterActions = turns.getWaiterActions();
            Action waiter = waiterActions.size() > 1 ? waiterActions.get(1) : waitAction();
            List<Action> chefActions = turns.getChefActions(waiter);
            Action chef = chefActions.size() > 1 ? chefActions.get(1) : waitAction();
            input.append(waiterActions.indexOf(waiter)).append('\n');
            input.append(chefActions.indexOf(chef)).append('\n');
            trace.add(turn + ": " + waiter.task() + " " + waiter.customerId() + " / "
                    + chef.task() + " " + chef.customerId());
            turns.completeTurn(waiter, chef);
        }
        check(simulation.getPaidCustomerCount() == 5 && simulation.getRevenueCents() == 6900
                && simulation.getAvgSatisfaction() == 74.4, "Reproducible victory scores: 5, $69, 74.4; actual: "
                + simulation.getPaidCustomerCount() + ", " + simulation.getRevenue() + ", " + simulation.getAvgSatisfaction());
        check(simulation.getArrivalCount() == 6 && turns.getCompletedTurns() == 18,
                "All six scheduled arrivals and eighteen turns");
        check(simulation.getCustomer(6).getStatus() != CustomerStatus.PAID,
                "Turn-16 arrival cannot pay by turn 18");
        expect(IllegalStateException.class, () -> turns.beginTurn(19));
        replay(input.toString(), true, 5, 6900, 74.4);
        replay("0\n0\n".repeat(18), false, 0, 0, 0);

        Files.createDirectories(Path.of("build"));
        Files.writeString(Path.of("build/victory-input.txt"), input);
        Files.writeString(Path.of("build/defeat-input.txt"), "0\n0\n".repeat(18));
        Files.write(Path.of("build/victory-actions.txt"), trace);
    }

    private static void replay(String choices, boolean victory, int paid, int cents, double average) {
        Restaurant restaurant = new Restaurant();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Scanner input = new Scanner(choices); PrintStream stream = new PrintStream(output)) {
            GameController game = new GameController(restaurant, new TurnController(restaurant), new GameUI(input, stream));
            check(!game.isVictory(), "No victory before session");
            game.startGame();
            check(game.getCurrentTurn() == 18 && game.isVictory() == victory, "Session completes with expected result");
            check(restaurant.getPaidCustomerCount() == paid && restaurant.getRevenueCents() == cents
                    && restaurant.getAvgSatisfaction() == average, "Replay scoring matches");
            check(restaurant.getArrivalCount() == 6, "Replay arrivals");
            expect(IllegalStateException.class, game::startGame);
        }
        String text = output.toString(StandardCharsets.UTF_8);
        check(text.split("Restaurant Rush \\| Turn ", -1).length - 1 == 18, "Exactly eighteen start displays");
        check(text.indexOf(victory ? "Victory!" : "Defeat.") > text.indexOf("Turns used: 18/18"),
                "Final result appears only after turn eighteen");
        if (victory) {
            check(text.contains("Customer 1 selected Pasta.") && text.contains("Average paid satisfaction: 74.4"),
                    "UI shows customer choice and final paid average");
        } else {
            check(restaurant.getUnhappyCustomers() == 2, "Wait-only run records two abandonments");
        }
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) { throw new AssertionError(message); }
    }

    private static void expect(Class<? extends Throwable> expected, Runnable action) {
        checks++;
        try {
            action.run();
        } catch (Throwable exception) {
            if (expected.isInstance(exception)) { return; }
            throw new AssertionError("Expected " + expected.getSimpleName(), exception);
        }
        throw new AssertionError("Expected " + expected.getSimpleName());
    }
}
