package restaurantrush.entity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;


/** Aggregate holding restaurant state. Collections are exposed as immutable snapshots. */
public final class Restaurant {
   public static final int TABLE_COUNT = 2;
   public static final int MAX_ORDERS = 6;
   private int revenue = 0;
   private final List<Table> tables = List.of(new Table(1), new Table(2));
   private final List<Order> orders = new ArrayList<>();
   private final Waiter waiter = new Waiter();
   private final Chef chef = new Chef();
   private final Cashier cashier = new Cashier();
   private final Queue<Customer> waitingQueue = new ArrayDeque<>();
   private final Queue<Customer> paymentQueue = new ArrayDeque<>();
   private int unhappyDepartures = 0;
   private int servedCustomers = 0;
   private int paidCustomers = 0;
   private int totalPaidSatisfaction = 0;

   public int getRevenue() {
       return revenue;
   }
   public List<Table> getTables() {
       return tables;
   }
   public List<Order> getOrders() {
       return List.copyOf(orders);
   }
  
   public List<Customer> getWaitingCustomers() {
       return List.copyOf(waitingQueue);
   }
   public List<Customer> getPayingCustomers() {
       return List.copyOf(paymentQueue);
   }


   public void addOrder(Order order) {
       if (order == null || orders.contains(order)) {
           throw new IllegalArgumentException("Invalid or duplicate order");
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
        if (customer == null) {
        throw new IllegalArgumentException("Customer cannot be null");
        }

        if (customer.getStatus() != CustomerStatus.WAITING) {
            throw new IllegalArgumentException("Customer must be waiting");
        }

        waitingQueue.add(customer);
   }


   public Customer getWaitingCustomer() {
       return waitingQueue.peek();
   }

   public boolean removeWaitingCustomer(Customer customer) {
       return waitingQueue.remove(customer);
   }


   public List<Order> getPendingOrders() {
       List<Order> pendingOrders = new ArrayList<>();

        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.PREPARING) {
                pendingOrders.add(order);
            }
        }

        return pendingOrders;
   }


   public void enqueueForPayment(Customer customer) {
        if (customer == null) {
        throw new IllegalArgumentException("Customer cannot be null");
        }

        if (customer.getStatus() != CustomerStatus.READY_TO_PAY) {
            throw new IllegalStateException("Customer is not ready to pay");
        }

        if (paymentQueue.contains(customer)) {
            throw new IllegalStateException("Customer already in queue");
        }

        paymentQueue.add(customer);
   }


   /** Non-destructive peek: process payment before removing the customer. Null means empty. */
   public Customer getNextPayingCustomer() {
       return paymentQueue.peek();
   }
   public boolean removePayingCustomer(Customer customer) {
       return paymentQueue.remove(customer);
   }


   /** Returns null if all tables are occupied. */
   public Table getAvailableTable() {
    for (Table table : tables) {
        if (table.isAvailable()) {
            return table;
        }
    }
    return null;
   }


   /** Records payment once; repeated calls fail before revenue changes. */
   public void updateRevenue(Customer customer) {
       revenue += order.getPrice();
       servedCustomers++;
   }


   /** Provisional metric: current waiting and seated customers, deduplicated; empty means 0. */
   public double getAvgSatisfaction() {
       if (paidCustomers == 0) {
           return 0.0;
       }
       double avgSatisfaction = (double) totalPaidSatisfaction / paidCustomers;
       return avgSatisfaction;
   }


   /** TODO: make departure recording atomic with removal/cancellation in the turn workflow. */
   public void recordUnhappyDeparture() {
       unhappyDepartures++;
   }
   public int getUnhappyCustomers() {
       return unhappyDepartures;
   }
   public int getServedCustomers() {
       return servedCustomers; 
   }


   public Waiter getWaiter() {
       return waiter;
   }


   public Chef getChef() {
       return chef;
   }


   public Cashier getCashier() {
       return cashier;
   }
}


