package exchangetask.exchange;

import exchangetask.ExchangeInterface;
import exchangetask.QueryInterface;
import exchangetask.exception.RequestRejectedException;
import exchangetask.order.Order;
import exchangetask.order.Side;

import java.util.*;
import java.util.function.Predicate;

/**
 * Represent the centralized trading venue which processes buy and sell orders sent by traders.
 * Init a empty map to hold orders when construct
 */
public class Exchange implements ExchangeInterface, QueryInterface {
    private Map<Long, Order> restingOrders;
    private static final int EXECUTED_ORDER_SIZE = 0;

    public Exchange( ) {
        restingOrders = new HashMap<>();
    }

    /**
     * Method for test purpose
     */
    public Map<Long, Order> getRestingOrders() {
        return restingOrders;
    }

    /**
     * Send a trader order to current exchanger
     * Save newly create order to restingOrders if it cannot process
     * or immediately process order
     * @param orderId - unique id of future created order
     * @param isBuy - logical value represent is order buy or sell
     * @param price - uses for future matching search
     * @param size - number of assets to buy or sell
     * @throws RequestRejectedException if init param isn't valid
     */
    public void send(long orderId, boolean isBuy, int price, int size) throws RequestRejectedException {
        if(restingOrders.containsKey(orderId))
            throw new RequestRejectedException("Id of order must be unique");

        if(price <= 0 || size <= 0)
            throw new RequestRejectedException("Price and size cannot be less than zero");


        Side side = isBuy ? Side.BUY : Side.SELL;
        Order order = new Order(orderId, side, price, size);


        if(restingOrders.isEmpty())
            restingOrders.put(orderId, order);
        else {
            try {
                processOrder(order);

                if (order.getSize() != EXECUTED_ORDER_SIZE)
                    restingOrders.put(orderId, order);
            } catch (RequestRejectedException e) {
                restingOrders.put(orderId, order);
            }
        }
    }

    /**
     * Helper method to process Order
     * @param order - valid order to process
     * @throws RequestRejectedException if inner function throw exception
     */
    private void processOrder(Order order) throws RequestRejectedException {
        int totalSizeAtPrice = getTotalSizeAtPrice(order.getPrice(), order.getSide());

        while(totalSizeAtPrice > 0 && order.getSize() > 0) {
            Order firstMatchedOrder = getFirstMatchedOrder(order.getSide());

            executeOrderWithSmallerSize(order, firstMatchedOrder);

            if(firstMatchedOrder.getSize() == EXECUTED_ORDER_SIZE)
                restingOrders.remove(firstMatchedOrder.getId());
        }
    }

    /**
     * Helper method to execute order with smaller price depending on it's side
     * @throws RequestRejectedException if orders side is equals
     */
    private void executeOrderWithSmallerSize(Order o1, Order o2) throws RequestRejectedException {
        if(o1.getSide().equals(o2.getSide()))
            throw new RequestRejectedException("Orders sides must be opposite to each other");

        int minSize = Integer.min(o1.getSize(), o2.getSize());
        decreaseSize(o1, o2, minSize);
    }

    /**
     * Decrease size from orders
     */
    private void decreaseSize(Order o1, Order o2, int sizeToDecrease) {
        o1.setSize(o1.getSize() - sizeToDecrease);
        o2.setSize(o2.getSize() - sizeToDecrease);
    }

    /**
     * Util method to find first matched order
     * @param side - trigger param to select correct function
     * @return searched order
     * @throws RequestRejectedException if computing inner function throw exception
     */
    private Order getFirstMatchedOrder(Side side) throws RequestRejectedException {
        return side.equals(Side.BUY) ? getLowestSellPrice() : getHighestBuyPrice();

    }

    /**
     * Modify order by id
     * @param orderId - id of already existed order
     * @param price - param to modify
     * @param size - param size to modify
     * @throws RequestRejectedException if restingOrders doesn't contain order with id
     */
    public void modify(long orderId, int price, int size) throws RequestRejectedException {
        if(!restingOrders.containsKey(orderId))
            throw new RequestRejectedException();

        Order order = restingOrders.get(orderId);
        order.setPrice(price);
        order.setSize(size);
    }

    /**
     * Cancel already created order
     * @param orderId - id to find
     * @throws RequestRejectedException if order doesn't exist
     */
    public void cancel(long orderId) throws RequestRejectedException {
        if(!restingOrders.containsKey(orderId))
            throw new RequestRejectedException("Cannot find a order with id: " + orderId);

        restingOrders.remove(orderId);
    }

    /**
     * Sum a total size of orders where price is in range from 0 to price
     * @param price - upper bound for search function
     * @param side - trigger param to init predicate function
     * @return sum of sizes or 0 if nothing was find
     * @throws RequestRejectedException if input price if less than zero
     */
    public int getTotalSizeAtPrice(int price, Side side) throws RequestRejectedException {
        if(price < 0 )
            throw new RequestRejectedException("Price cannot be less than zero");

        Predicate<Order> predicateFunction = getPredicateFunction(side, price);

        return restingOrders.values().stream()
                .filter(predicateFunction)
                .mapToInt(Order::getSize)
                .sum();
    }

    /**
     * Util method to init predicate function depending on side param
     * @param side - trigger param to select function
     */
    private Predicate<Order> getPredicateFunction(Side side, int price) {
        return side.equals(Side.BUY) ? (order -> price >= order.getPrice()) : (order -> price <= order.getPrice());
    }


    /**
     * @return order with max buy price
     * @throws RequestRejectedException if nothing was find
     */
    public Order getHighestBuyPrice() throws RequestRejectedException {
        Optional<Order> orderWithHigherPrice = restingOrders.values().stream()
                .filter((order) -> order.getSide().equals(Side.BUY))
                .max(Comparator.comparingInt(Order::getPrice));

        return orderWithHigherPrice.orElseThrow(() -> new RequestRejectedException("No Buy Order in restingOrders"));
    }

    /**
     * @return order with min sell price
     * @throws RequestRejectedException if nothing was find
     */
    public Order getLowestSellPrice() throws RequestRejectedException {
        Optional<Order> orderWithLowerPrice = restingOrders.values().stream()
                .filter((order) -> order.getSide().equals(Side.SELL))
                .min(Comparator.comparingInt(Order::getPrice));

        return orderWithLowerPrice.orElseThrow(() -> new RequestRejectedException("No Sell Order in restingOrders"));
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        restingOrders.values().forEach(result::append);

        return result.toString();
    }
}
