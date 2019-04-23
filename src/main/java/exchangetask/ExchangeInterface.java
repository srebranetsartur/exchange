package exchangetask;

import exchangetask.exception.RequestRejectedException;

public interface ExchangeInterface {
    void send(long orderId, boolean isBuy, int price, int size) throws RequestRejectedException;
    void modify(long orderId, int price, int size) throws RequestRejectedException;
    void cancel(long orderId) throws RequestRejectedException;
}

