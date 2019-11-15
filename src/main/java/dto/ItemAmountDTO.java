package dto;

public class ItemAmountDTO {
    private long id;
    private long amount = 0;
    private long orderId;

    public ItemAmountDTO() {}

    public ItemAmountDTO(long id, long amount, long orderId) {
        this.id = id;
        this.amount = amount;
        this.orderId = orderId;
    }

    public long getId() {
        return this.id;
    }

    private void setId(long id) {
        this.id = id;
    }

    public long getAmount() {
        return this.amount;
    }

    private void setAmount(long amount) {
        this.amount = amount;
    }

    public long getOrderId() {
        return this.id;
    }

    private void setOrderId(long id) {
        this.id = id;
    }
}