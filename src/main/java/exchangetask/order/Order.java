package exchangetask.order;

import java.util.Objects;

public class Order {
    private Long id;
    private Side side;
    private int price;
    private int size;

    public Order() {}

    public Order(Long id, Side side, int price, int size) {
        this.id = id;
        this.side = side;
        this.price = price;
        this.size = size;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return price == order.price &&
                size == order.size &&
                side == order.side;
    }

    @Override
    public int hashCode() {
        return Objects.hash(side, price, size);
    }

    @Override
    public String toString() {
        return String.format("Order#%d: {side: %s, price: %d, size: %d}", id, side.toString(), price, size);
    }
}
