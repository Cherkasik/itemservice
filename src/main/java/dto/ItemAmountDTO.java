package dto;

public class ItemAmountDTO {
    private long id;
    private long amount = 0;
    private Long orderId;
    private String type;

    public ItemAmountDTO() {}

    public ItemAmountDTO(long id, long amount, Long orderId, String type) {
        this.id = id;
        this.amount = amount;
        this.orderId = orderId;
        this.type = type;
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

    private void setOrderId(Long id) {
        this.id = id;
    }

    public long getType() {
        return this.type;
    }

    private void setType(String type) {
        this.type = type;
    }
}