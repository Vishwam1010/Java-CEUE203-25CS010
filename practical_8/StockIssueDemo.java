import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class OutOfStockException extends Exception {
    private final int shortfall;

    public OutOfStockException(int shortfall) {
        super("Not enough stock. Short by " + shortfall + " unit(s).");
        this.shortfall = shortfall;
    }

    public int getShortfall() {
        return shortfall;
    }
}

class InvalidQuantityException extends Exception {
    public InvalidQuantityException(int quantity) {
        super("Quantity must be greater than zero. Received: " + quantity);
    }
}

class Warehouse {
    private final Map<String, Integer> inventory = new HashMap<>();

    public void addStock(String item, int quantity) {
        int currentStock = inventory.getOrDefault(item, 0);
        inventory.put(item, currentStock + quantity);
    }

    public void issue(String item, int quantity)
            throws OutOfStockException, InvalidQuantityException {
        if (quantity <= 0) {
            throw new InvalidQuantityException(quantity);
        }

        int available = inventory.getOrDefault(item, 0);

        if (quantity > available) {
            throw new OutOfStockException(quantity - available);
        }

        inventory.put(item, available - quantity);
        System.out.println("Issued " + quantity + " unit(s) of " + item + ".");
    }
}

class StockRequest {
    private final String item;
    private final int quantity;

    public StockRequest(String item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public String getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }
}

public class StockIssueDemo {
    public static void main(String[] args) {
        Warehouse warehouse = new Warehouse();
        warehouse.addStock("Keyboard", 5);
        warehouse.addStock("Mouse", 3);

        List<StockRequest> requests = new ArrayList<>();
        requests.add(new StockRequest("Keyboard", 2));
        requests.add(new StockRequest("Mouse", 5));
        requests.add(new StockRequest("Monitor", 1));
        requests.add(new StockRequest("Keyboard", 0));
        requests.add(new StockRequest("Keyboard", 3));

        for (StockRequest request : requests) {
            try {
                warehouse.issue(request.getItem(), request.getQuantity());
            } catch (OutOfStockException e) {
                System.out.println("Could not issue "
                        + request.getItem() + ": " + e.getMessage());
                System.out.println("Shortfall: " + e.getShortfall()
                        + " unit(s).");
            } catch (InvalidQuantityException e) {
                System.out.println("Could not issue "
                        + request.getItem() + ": " + e.getMessage());
            }
        }

        System.out.println("Finished processing all requests.");
    }
}