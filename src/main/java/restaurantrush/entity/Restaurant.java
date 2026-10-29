package restaurantrush.entity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.Queue;

public final class Restaurant {
    public static final int TABLE_COUNT = 2;
    public static final int MAX_ORDERS = 6;
    private int revenueCents;
    private final List<Table> tables = List.of(new Table(1), new Table(2));
    private final List<Order> orders = new ArrayList<>();
    // Retain registered IDs after departure to prevent duplicate arrivals.
    private final Map<Integer, Customer> customers = new LinkedHashMap<>();
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private final Menu menu = new Menu();
    private final Queue<Customer> waitingQueue = new ArrayDeque<>();
    private final Queue<Customer> paymentQueue = new ArrayDeque<>();
    private int unhappyDepartures;
    private int servedCustomers;
    private int paidCustomers;
    private int totalPaidSatisfaction;

    public double getRevenue() { return revenueCents / 100.0; }
    public int getRevenueCents() { return revenueCents; }
    public List<Table> getTables() { return tables; }
    public List<Order> getOrders() { return List.copyOf(orders); }
    public Waiter getWaiter() { return waiter; }
    public Chef getChef() { return chef; }
    public Cashier getCashier() { return cashier; }
    public List<Customer> getWaitingCustomers() { return List.copyOf(waitingQueue); }
    public List<Customer> getPayingCustomers() { return List.copyOf(paymentQueue); }
    public int getArrivalCount() { return customers.size(); }
    public int getUnhappyCustomers() { return unhappyDepartures; }
    /** Meals delivered, including customers who have not paid yet. */
    public int getServedCustomers() { return servedCustomers; }
    public int getPaidCustomerCount() { return paidCustomers; }

    public List<Customer> getCustomers() {
        return customers.values().stream().filter(customer ->
                customer.getStatus() != CustomerStatus.PAID
                && customer.getStatus() != CustomerStatus.CANCELLED).toList();
    }

    public Customer getCustomer(int id) {
        Customer customer = customers.get(id);
        if (customer == null) { throw new IllegalArgumentException("Unknown customer " + id); }
        return customer;
    }

    public OptionalDouble getPaidSatisfactionAverage() {
        return paidCustomers == 0 ? OptionalDouble.empty()
                : OptionalDouble.of((double) totalPaidSatisfaction / paidCustomers);
    }

    public double getAvgSatisfaction() { return getPaidSatisfactionAverage().orElse(0); }

    public void addWaitingCustomer(Customer customer) {
        Objects.requireNonNull(customer);
        if (customer.getStatus() != CustomerStatus.WAITING || customer.getTable() != null
                || customers.containsKey(customer.getCustomerId()) || customers.size() >= 6) {
            throw new IllegalArgumentException("Expected a new waiting customer within the six-arrival limit");
        }
        customers.put(customer.getCustomerId(), customer);
        waitingQueue.add(customer);
    }

    public Table getAvailableTable() {
        return tables.stream().filter(Table::isAvailable).findFirst().orElse(null);
    }

    public List<Order> getPendingOrders() {
        return orders.stream().filter(order -> order.getStatus() == OrderStatus.PREPARING).toList();
    }

    public void seatCustomer(int id) {
        Customer customer = getCustomer(id);
        if (!waitingQueue.contains(customer)) { throw new IllegalStateException("Customer is not in the waiting queue"); }
        Table table = getAvailableTable();
        if (table == null) { throw new IllegalStateException("All tables are occupied"); }
        waiter.sitCustomer(customer, table);
        waitingQueue.remove(customer);
    }

    public Order takeOrder(int id) {
        Customer customer = getCustomer(id);
        if (!waiter.isAvailable() || customer.getStatus() != CustomerStatus.SEATED
                || customer.getOrder() != null || customer.getTable() == null
                || customer.getTable().getCustomer() != customer || orders.size() >= MAX_ORDERS) {
            throw new IllegalStateException("Cannot take this customer's order");
        }
        Order order = new Order(customer.getTable().getTableId(), List.of(menu.chooseForCustomer(id)));
        customer.setOrder(order);
        orders.add(order);
        waiter.takeOrder(customer);
        return order;
    }

    public void serveCustomer(int id, int turn) {
        Customer customer = getCustomer(id);
        if (turn < 1 || customer.getStatus() != CustomerStatus.ORDERED
                || customer.getOrder() == null || !orders.contains(customer.getOrder())) {
            throw new IllegalStateException("Customer has no registered order to serve");
        }
        waiter.serveOrder(customer.getOrder());
        customer.recordService(turn);
        servedCustomers++;
    }

    public void finishEating(int turn) {
        for (Customer customer : getCustomers()) {
            if (customer.getStatus() == CustomerStatus.ORDERED
                    && customer.getOrder().getStatus() == OrderStatus.SERVED
                    && customer.getServedTurn() < turn) {
                customer.finishEating(turn);
                paymentQueue.add(customer);
            }
        }
    }

    /** Returns only the oldest customer who was ready before this turn. */
    public Customer getEligiblePayingCustomer(int turn) {
        Customer customer = paymentQueue.peek();
        return customer != null && customer.getReadyToPayTurn() < turn ? customer : null;
    }

    public Customer getNextPayingCustomer() { return paymentQueue.peek(); }

    /** Validates all prerequisites before recording a payment and cleaning up. */
    public void completePayment(Customer customer) {
        Objects.requireNonNull(customer);
        Order order = customer.getOrder();
        Table table = customer.getTable();
        if (customers.get(customer.getCustomerId()) != customer || paymentQueue.peek() != customer
                || customer.getStatus() != CustomerStatus.READY_TO_PAY
                || order == null || !orders.contains(order) || order.getStatus() != OrderStatus.SERVED
                || table == null || table.getCustomer() != customer) {
            throw new IllegalStateException("Expected the oldest queued customer with a served order and table");
        }
        int nextRevenue = Math.addExact(revenueCents, order.getPriceCents());
        order.markPaid();
        customer.setStatus(CustomerStatus.PAID);
        revenueCents = nextRevenue;
        paidCustomers++;
        totalPaidSatisfaction += customer.getSatisfaction();
        paymentQueue.remove();
        table.unAssign();
        orders.remove(order);
    }

    public void applyWaitingAndDepartures() {
        for (Customer customer : getCustomers()) {
            if (customer.isWaitingForService()) {
                customer.decreaseSatisfaction();
                if (customer.getSatisfaction() == 0) { abandon(customer); }
            }
        }
    }

    private void abandon(Customer customer) {
        Order order = customer.getOrder();
        if (order != null) {
            order.cancel();
            orders.remove(order);
        }
        waitingQueue.remove(customer);
        if (customer.getTable() != null) { customer.getTable().unAssign(); }
        customer.setStatus(CustomerStatus.CANCELLED);
        unhappyDepartures++;
    }
}
