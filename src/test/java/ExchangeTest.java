import exchangetask.exception.RequestRejectedException;
import exchangetask.exchange.Exchange;
import exchangetask.order.Order;
import exchangetask.order.Side;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExchangeTest {
    private static Exchange exchange;
    @BeforeAll
    static void init() {
        exchange = new Exchange();
    }

    @Test
    void testInsertOrder() throws RequestRejectedException {
        exchange.send(1L, true, 10, 5);
        assertEquals(1, exchange.getRestingOrders().size());
    }

    @Test
    void shouldThrowExceptionBecausePriceEqualsZero() {
        assertThrows(RequestRejectedException.class,() -> exchange.send(1L, true, -2, 5));
    }

    @Test
    void testModifyMethod() throws RequestRejectedException {
        exchange.send(1L, true, 10, 5);
        exchange.modify(1L, 12, 6);
        assertEquals(exchange.getRestingOrders().get(1L), new Order(1L , Side.BUY, 12, 6));
    }

    @Test
    void shouldThrowException_WhenTryModify_NotExistedId() {
        assertThrows(RequestRejectedException.class, () -> exchange.modify(2L, 20, 10));
    }

    @Test
    void testCancelMethod() throws RequestRejectedException {
        exchange.send(1L, true, 10, 5);
        exchange.cancel(1L);
        assertTrue(() -> exchange.getRestingOrders().isEmpty());
    }

    @Test
    void shouldThrowException_WhenTryCancel_NotExistedId() {
        assertThrows(RequestRejectedException.class, () -> exchange.cancel(2L));
    }

    @Test
    void shouldProcessOrderInstantly() throws RequestRejectedException {
        exchange.send(1L, true, 10, 5);
        exchange.send(2L , true, 12,7);
        exchange.send(3L, true, 6, 2);
        exchange.send(4L, true, 5, 1);

        exchange.send(5L, false, 5, 5);

        assertTrue(exchange.getRestingOrders().containsValue(new Order(2L, Side.BUY, 12, 2)));
    }

    @Test
    void shouldSaveOrderForFutureMatch() throws RequestRejectedException {
        exchange.send(1L, true, 10, 5);
        exchange.send(2L , true, 10, 4);
        exchange.send(3L, false, 11, 2);

        assertEquals(3, exchange.getRestingOrders().size());
    }

    @Test
    void shouldReturnOrderWithMaxBuyPrice() throws RequestRejectedException {
        exchange.send(1L, true, 10, 5);
        exchange.send(2L , true, 12,7);
        exchange.send(3L, true, 6, 2);
        exchange.send(4L, true, 5, 1);
        exchange.send(5L, false, 13, 5);

        assertEquals(new Order(2L, Side.BUY, 12, 7), exchange.getHighestBuyPrice());
    }

    @Test
    void shouldReturnOrderWithMinSellPrice() throws RequestRejectedException {
        exchange.send(1L, false, 10, 5);
        exchange.send(2L , false, 12,7);
        exchange.send(3L, false, 6, 2);
        exchange.send(4L, false, 5, 1);
        exchange.send(5L, true, 13, 5);

        assertEquals(new Order(4L, Side.SELL, 5, 1), exchange.getLowestSellPrice());
    }



}
