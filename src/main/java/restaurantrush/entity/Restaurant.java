package restaurantrush.entity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/** Aggregate holding restaurant state. Collections are exposed as immutable snapshots. */
public final class Restaurant {
    public static final int TABLE_COUNT = 2;
    public static final int MAX_ORDERS = 6;
    private double revenue;
    private final List<Table> tables = List.of(new Table(1), new Table(2));
    private final List<Order> orders = new ArrayList<>();
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private final Queue<Customer> waitingQueue = new ArrayDeque<>();
    private final Queue<Customer> paymentQueue = new ArrayDeque<>();
    private final Set<Integer> knownCustomerIds = new HashSet<>();
    private int unhappyDepartures;
    private int servedCustomers;

    public double getRevenue() { return revenue; }
    public List<Table> getTables() { return tables; }
    public List<Order> getOrders() { return List.copyOf(orders); }
    public Waiter getWaiter() { return waiter; }
    public Chef getChef() { return chef; }
    public Cashier getCashier() { return cashier; }
    public List<Customer> getWaitingCustomers() { return List.copyOf(waitingQueue); }
    public List<Customer> getPayingCustomers() { return List.copyOf(paymentQueue); }

    public void addOrder(Order order) {
        Objects.requireNonNull(order);
        if (orders.size() >= MAX_ORDERS || orders.contains(order)) {
            throw new IllegalStateException("Order capacity reached or order already registered");
        }
        if (tables.stream().noneMatch(table -> table.getTableId() == order.getTableId())) {
            throw new IllegalArgumentException("Unknown table");
        }
        orders.add(order);
    }

    /** Release a closed order's slot; archive it separately if history is later required. */
    public void removeClosedOrder(Order order) {
        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order must be closed before removal");
        }
        orders.remove(order);
    }

    public void addWaitingCustomer(Customer customer) {
        Objects.requireNonNull(customer);
        if (customer.getStatus() != CustomerStatus.WAITING || !knownCustomerIds.add(customer.getCustomerId())) {
            throw new IllegalArgumentException("Expected a new waiting customer with a unique ID");
        }
        waitingQueue.add(customer);
    }

    public Customer peekWaitingCustomer() { return waitingQueue.peek(); }
    public Customer pollWaitingCustomer() { return waitingQueue.poll(); }
    public boolean removeWaitingCustomer(Customer customer) { return waitingQueue.remove(customer); }

    public List<Order> getPendingOrders() {
        return orders.stream().filter(order -> order.getStatus() == OrderStatus.PREPARING).toList();
    }

    public void enqueueForPayment(Customer customer) {
        Objects.requireNonNull(customer);
        if (customer.getStatus() != CustomerStatus.READY_TO_PAY || paymentQueue.contains(customer)) {
            throw new IllegalStateException("Customer must be ready to pay and not already queued");
        }
        paymentQueue.add(customer);
    }

    /** Non-destructive peek: process payment before removing the customer. Null means empty. */
    public Customer getNextPayingCustomer() { return paymentQueue.peek(); }
    public boolean removePayingCustomer(Customer customer) { return paymentQueue.remove(customer); }

    /** Returns null if all tables are occupied. */
    public Table getAvailableTable() {
        return tables.stream().filter(Table::isAvailable).findFirst().orElse(null);
    }

    /** Records payment once; repeated calls fail before revenue changes. */
    public void updateRevenue(Customer customer) {
        Objects.requireNonNull(customer);
        Order order = customer.getOrder();
        if (customer.getStatus() != CustomerStatus.READY_TO_PAY || order == null
                || !orders.contains(order) || order.getStatus() != OrderStatus.SERVED) {
            throw new IllegalStateException("Customer must have a registered served order and be ready to pay");
        }
        order.markPaid();
        customer.setStatus(CustomerStatus.PAID);
        revenue += order.getPrice();
        servedCustomers++;
    }

    /** Provisional metric: current waiting and seated customers, deduplicated; empty means 0. */
    public double getAvgSatisfaction() {
        Set<Customer> customers = new HashSet<>(waitingQueue);
        for (Table table : tables) {
            if (table.getCustomer() != null) { customers.add(table.getCustomer()); }
        }
        customers.addAll(paymentQueue);
        return customers.stream().mapToInt(Customer::getSatisfaction).average().orElse(0);
    }

    /** TODO: make departure recording atomic with removal/cancellation in the turn workflow. */
    public void recordUnhappyDeparture() { unhappyDepartures++; }
    public int getUnhappyCustomers() { return unhappyDepartures; }
    public int getServedCustomers() { return servedCustomers; }
}
