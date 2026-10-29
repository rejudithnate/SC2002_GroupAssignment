package restaurantrush.entity;

import java.util.List;

public final class Menu {
    private final List<MenuItem> menuItems;

    /** The diagram specifies three items; supply their actual values from the specification. */
    public Menu(List<MenuItem> menuItems) {
        this.menuItems = List.copyOf(menuItems);
        if (this.menuItems.size() != 3) {
            throw new IllegalArgumentException("The menu must contain three items");
        }
    }

    public List<MenuItem> getMenuItems() { return menuItems; }
}
