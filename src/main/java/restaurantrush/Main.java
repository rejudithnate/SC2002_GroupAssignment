package restaurantrush;

import java.util.Scanner;
import restaurantrush.boundary.GameUI;
import restaurantrush.control.GameController;
import restaurantrush.control.TurnController;
import restaurantrush.entity.Restaurant;

/** Composition root: constructs and connects the application dependencies. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        Restaurant restaurant = new Restaurant();
        try (Scanner scanner = new Scanner(System.in)) {
            GameUI ui = new GameUI(scanner, System.out);
            TurnController turns = new TurnController(restaurant);
            GameController game = new GameController(restaurant, turns, ui);
            game.startGame();
            System.out.println("Skeleton ready. Gameplay, menu data and victory rules are TODOs; see docs/TASKS.md.");
        }
    }
}
