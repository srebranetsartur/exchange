package exchangetask;

import exchangetask.exception.RequestRejectedException;
import exchangetask.order.Order;
import exchangetask.order.Side;

public interface QueryInterface {
    //Return sum of sizes of resting orders at <price> or zero
    int getTotalSizeAtPrice(int price, Side side) throws RequestRejectedException;

    //Return the highest price with at least one resting Buy order
    Order getHighestBuyPrice() throws RequestRejectedException;

    //Return the lowest price with at least one resting Sell order
    Order getLowestSellPrice() throws RequestRejectedException;
}
