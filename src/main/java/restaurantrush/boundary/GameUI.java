package restaurantrush.boundary;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import restaurantrush.control.TurnController.Action;
import restaurantrush.entity.Customer;
import restaurantrush.entity.Restaurant;
import restaurantrush.entity.Table;

/** Console presentation and input parsing. Available actions come from the controller. */
public final class GameUI {
    private final Scanner scanner;
    private final PrintStream output;

    public GameUI(Scanner scanner, PrintStream output) {
        this.scanner = Objects.requireNonNull(scanner);
        this.output = Objects.requireNonNull(output);
    }

    public void displayGameState(Restaurant restaurant, int turn, int maxTurns) {
        output.printf("Restaurant Rush | Turn %d/%d%n", turn, maxTurns);
        displayMetrics(restaurant);
        output.println("Waiting queue:");
        if (restaurant.getWaitingCustomers().isEmpty()) { output.println("  Empty"); }
        for (Customer customer : restaurant.getWaitingCustomers()) { displayCustomer(customer); }
        for (Table table : restaurant.getTables()) {
            output.printf("Table %d: %s%n", table.getTableId(), table.isAvailable() ? "Available" : "Occupied");
            if (table.getCustomer() != null) { displayCustomer(table.getCustomer()); }
        }
    }

    private void displayCustomer(Customer customer) {
        output.printf("  Customer %d | Satisfaction %d | %s",
                customer.getCustomerId(), customer.getSatisfaction(), customer.getStatus());
        if (customer.getOrder() != null) {
            output.printf(" | %s: %s (%d preparation units completed)",
                    customer.getOrder().getMenuItems().get(0).getName(),
                    customer.getOrder().getStatus(), customer.getOrder().getCookedUnit());
        }
        output.println();
    }

    /** Empty means end-of-input. Invalid syntax or unavailable options are retried. */
    public Optional<Action> readAction(String staff, List<Action> actions) {
        if (actions.isEmpty()) { throw new IllegalArgumentException("An action list must include Wait"); }
        output.println(staff + " tasks:");
        for (int index = 0; index < actions.size(); index++) {
            Action action = actions.get(index);
            String label = switch (action.task()) {
                case WAIT -> "Wait";
                case SEAT -> "Seat customer " + action.customerId();
                case TAKE_ORDER -> "Take order for customer " + action.customerId();
                case SERVE -> "Serve customer " + action.customerId();
                case PREPARE -> "Prepare one unit for customer " + action.customerId();
            };
            output.printf("  %d: %s%n", index, label);
        }
        while (true) {
            output.print(staff + " choice: ");
            if (!scanner.hasNextLine()) { return Optional.empty(); }
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice >= 0 && choice < actions.size()) { return Optional.of(actions.get(choice)); }
                output.println("That task is unavailable. Choose a displayed number.");
            } catch (NumberFormatException exception) {
                output.println("Please enter a whole number.");
            }
        }
    }

    public void displayFoodSelection(Customer customer) {
        output.printf("Customer %d selected %s.%n",
                customer.getCustomerId(), customer.getOrder().getMenuItems().get(0).getName());
    }

    public void displayTurnSummary(Restaurant restaurant, int turn, int maxTurns) {
        output.printf("End of turn | Turns used: %d/%d%n", turn, maxTurns);
        displayMetrics(restaurant);
        output.println();
    }

    private void displayMetrics(Restaurant restaurant) {
        String average = restaurant.getPaidSatisfactionAverage().isPresent()
                ? String.format(Locale.ROOT, "%.1f", restaurant.getAvgSatisfaction()) : "N/A";
        output.printf(Locale.ROOT,
                "Revenue: $%.2f | Paid: %d | Average paid satisfaction: %s | Served: %d | Unhappy departures: %d%n",
                restaurant.getRevenue(), restaurant.getPaidCustomerCount(), average,
                restaurant.getServedCustomers(), restaurant.getUnhappyCustomers());
    }

    public void displayResult(boolean victory, Restaurant restaurant) {
        output.println(victory ? "Victory!" : "Defeat.");
        displayMetrics(restaurant);
        output.println("Targets: 4 paid customers, $45.00 revenue, 60 average paid satisfaction.");
    }

    public void displayInputEnded() {
        output.println("Input ended. Game stopped before completion; no final result.");
    }
}