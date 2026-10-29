package restaurantrush.control;

import java.util.Objects;
import restaurantrush.boundary.GameUI;
import restaurantrush.entity.Restaurant;

/** Session orchestration. This skeleton intentionally does not implement the game loop. */
public final class GameController {
    public static final int MAX_TURN = 18;
    private int currentTurn = 1;
    private final Restaurant restaurant;
    private final TurnController turnController;
    private final GameUI ui;

    public GameController(Restaurant restaurant, TurnController turnController, GameUI ui) {
        this.restaurant = Objects.requireNonNull(restaurant);
        this.turnController = Objects.requireNonNull(turnController);
        this.ui = Objects.requireNonNull(ui);
    }

    /** Displays initial state. TODO: implement the 18-turn loop and final result. */
    public void startGame() {

        while (currentTurn < MAX_TURN) {
            startTurn();
        }

        ui.displayResult(isVictory());
    }

    /** TODO: agree victory thresholds with the assignment specification. */
    public boolean isVictory() {
        return currentTurn == MAX_TURN
            && restaurant.getPaidCustomerCount() >= 4
            && restaurant.getRevenue() >= 45
            && restaurant.getAvgSatisfaction() >= 60;
    }

    public void setCurrentTurn(int turnNo) {
        if (turnNo < 1 || turnNo > MAX_TURN) {
            throw new IllegalArgumentException("Turn must be between 1 and " + MAX_TURN);
        }
        currentTurn = turnNo;
    }

    public int getCurrentTurn() { return currentTurn; }

    /** Processes the current turn. The future session loop owns advancing the turn number. */
    public void startTurn() {
        if (!started) {
            throw new IllegalStateException("Call startGame() first.");
        }

        if (currentTurn >= MAX_TURN) {
            throw new IllegalStateException("All turns are complete.");
        }

        currentTurn++;

        // Displays the state at the beginning of this turn.
        ui.displayGameState();

        // Handles arrivals, staff actions, payments, satisfaction, etc.
        turnController.runTurn(currentTurn);
    }
}

