package restaurantrush.control;

/** Output/input port owned by the control layer; implemented by a boundary. */
public interface GameView {
    void displayGameState(GameState state);
    int getWaiterChoice();
    int readChefChoice();
    void displayResult(boolean victory);
}
