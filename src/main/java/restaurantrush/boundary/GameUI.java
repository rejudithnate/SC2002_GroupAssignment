package restaurantrush.boundary;

import java.io.PrintStream;
import java.util.Objects;
import java.util.Scanner;
import restaurantrush.entity.Restaurant;

/** Console boundary: formatting and parsing only, never game rules. */
public final class GameUI {
    private final Scanner scanner;
    private final PrintStream output;

    public GameUI(Scanner scanner, PrintStream output) {
        this.scanner = Objects.requireNonNull(scanner);
        this.output = Objects.requireNonNull(output);
    }

    /** Reads state for display only; mutations belong to controls and entities. */
    public void displayGameState(Restaurant restaurant, int turn, int maxTurns) {
        output.printf("Restaurant Rush | Turn %d/%d%n", turn, maxTurns);
        output.printf("Revenue: %.2f | Average satisfaction: %.1f%n",
                restaurant.getRevenue(), restaurant.getAvgSatisfaction());
        output.printf("Waiting: %d | Pending orders: %d | Served: %d | Unhappy departures: %d%n",
                restaurant.getWaitingCustomers().size(), restaurant.getPendingOrders().size(),
                restaurant.getServedCustomers(), restaurant.getUnhappyCustomers());
    }

    public int getWaiterChoice() {
        return readInteger("Waiter action (action IDs are defined by the controller): ");
    }

    public int readChefChoice() {
        return readInteger("Chef action (action IDs are defined by the controller): ");
    }

    public void displayResult(boolean victory) {
        output.println(victory ? "Victory!" : "Game over.");
    }

    private int readInteger(String prompt) {
        while (true) {
            output.print(prompt);
            if (!scanner.hasNextLine()) {
                throw new IllegalStateException("Console input ended.");
            }
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException exception) {
                output.println("Please enter a whole number.");
            }
        }
    }
}
