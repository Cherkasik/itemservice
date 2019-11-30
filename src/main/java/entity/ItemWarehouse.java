package entity;

import entity.Item;

public class ItemWarehouse {
    private long id;
    private long amount = 0;
    private long reservedAmount = 0;

    public ItemWarehouse() {}

    public ItemWarehouse(Item item) {
        this.id = item.getId();
        this.amount = 0;
        this.reservedAmount = 0;
    }

    public ItemWarehouse(Item item, long amount, long reservedAmount) {
        this.id = item.getId();
        this.amount = amount;
        this.reservedAmount = reservedAmount;
    }

    public ItemWarehouse(Item item, long amount) {
        this.id = item.getId();
        this.amount = amount;
        this.reservedAmount = 0;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAmount() {
        return this.amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void changeAmount(long amount) {
        this.amount += amount;
    }

    public long getReservedAmount() {
        return this.reservedAmount;
    }

    public void setReservedAmount(long reservedAmount) {
        this.reservedAmount = reservedAmount;
    }

    public void changeReservedAmount(long reservedAmount) {
        this.reservedAmount += reservedAmount;
    }
}