package app;

import java.util.ArrayList;
import java.util.List;

public class InventoryService {
    private final List<Product> products = new ArrayList<>();

    public String performAction(String action, String productName) {
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null.");
        }

        switch (action.trim().toUpperCase()) {
            case "ADD":
                products.add(new Product(productName));
                return "Product added: " + productName;

            case "COUNT":
                return "Product count: " + products.size();

            default:
                throw new IllegalArgumentException("Invalid action: " + action);
        }
    }

    public int getCount() {
        return products.size();
    }

    public List<Product> getProducts() {
        return new ArrayList<>(products);
    }
}