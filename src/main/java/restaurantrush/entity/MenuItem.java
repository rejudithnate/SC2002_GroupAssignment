
package restaurantrush.entity;

import java.util.Objects;

public final class MenuItem {

    private final String itemName;
    private final double price;
    private final int preparationUnits;

    public MenuItem(String itemName, double price, int preparationUnits) {

        this.itemName = Objects.requireNonNull(itemName);

        if (itemName.isBlank() || !Double.isFinite(price)
                || price < 0 || preparationUnits < 1) {
            throw new IllegalArgumentException(
                "Menu items need a name, nonnegative price and positive preparation units"
            );
        }

        this.price = price;
        this.preparationUnits = preparationUnits;
    }

    public String getName() {
        return itemName;
    }

    public double getPrice() {
        return price;
    }

    public int getPreparationUnits() {
        return preparationUnits;
    }
}
