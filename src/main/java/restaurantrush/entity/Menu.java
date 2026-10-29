
package restaurantrush.entity;

import java.util.ArrayList;
import java.util.List;

public final class Menu {

    private final List<MenuItem> menuItems;

    public Menu() {
        menuItems = new ArrayList<>();

        menuItems.add(new MenuItem("Salad", 9.0, 1));
        menuItems.add(new MenuItem("Burger", 15.0, 1));
        menuItems.add(new MenuItem("Pasta", 18.0, 2));
    }

    public List<MenuItem> getMenuItems() {
        return new ArrayList<>(menuItems);
    }
}

