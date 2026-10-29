package restaurantrush.boundary;

import java.io.PrintStream;
import java.util.Objects;
import java.util.Scanner;
import restaurantrush.control.GameState;
import restaurantrush.control.GameView;

/** Console boundary: formatting and parsing only, never game rules. */
public final class GameUI implements GameView {
    private final Scanner scanner;
    private final PrintStream output;

    public GameUI(Scanner scanner, PrintStream output) {
        this.scanner = Objects.requireNonNull(scanner);
        this.output = Objects.requireNonNull(output);
    }

    @Override
    public void displayGameState(GameState state) {
        output.printf("Restaurant Rush | Turn %d/%d%n", state.turn(), state.maxTurns());
        output.printf("Revenue: %.2f | Average satisfaction: %.1f%n",
                state.revenue(), state.averageSatisfaction());
        output.printf("Waiting: %d | Pending orders: %d | Served: %d | Unhappy departures: %d%n",
                state.waitingCustomers(), state.pendingOrders(),
                state.servedCustomers(), state.unhappyDepartures());
    }

    @Override
    public int getWaiterChoice() {
        return readInteger("Waiter action (action IDs are defined by the controller): ");
    }

    @Override
    public int readChefChoice() {
        return readInteger("Chef action (action IDs are defined by the controller): ");
    }

    @Override
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
