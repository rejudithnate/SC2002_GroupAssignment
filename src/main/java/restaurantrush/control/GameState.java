package restaurantrush.control;

/** Immutable presentation snapshot; the boundary cannot mutate entities through it. */
public record GameState(int turn, int maxTurns, double revenue,
                        double averageSatisfaction, int waitingCustomers,
                        int pendingOrders, int servedCustomers,
                        int unhappyDepartures) { }
