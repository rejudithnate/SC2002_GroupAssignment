package restaurantrush.control;

import java.util.Objects;
import restaurantrush.entity.Restaurant;

/** Session orchestration. This skeleton intentionally does not implement the game loop. */
public final class GameController {
    public static final int MAX_TURN = 18;
    private int currentTurn = 1;
    private final Restaurant restaurant;
    private final TurnController turnController;
    private final GameView view;

    public GameController(Restaurant restaurant, TurnController turnController, GameView view) {
        this.restaurant = Objects.requireNonNull(restaurant);
        this.turnController = Objects.requireNonNull(turnController);
        this.view = Objects.requireNonNull(view);
    }

    /** Displays initial state. TODO: implement the 18-turn loop and final result. */
    public void startGame() {
        displayState();
    }

    /** TODO: agree victory thresholds with the assignment specification. */
    public boolean isVictory() {
        throw new UnsupportedOperationException("TODO: implement victory conditions");
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
        turnController.runTurn(currentTurn);
        displayState();
    }

    private void displayState() {
        view.displayGameState(new GameState(currentTurn, MAX_TURN,
                restaurant.getRevenue(), restaurant.getAvgSatisfaction(),
                restaurant.getWaitingCustomers().size(), restaurant.getPendingOrders().size(),
                restaurant.getServedCustomers(), restaurant.getUnhappyCustomers()));
    }
}
