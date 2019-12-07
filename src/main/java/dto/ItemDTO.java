package dto;

import entity.Item;

public class ItemDTO {
    private long id;
    private String name;
    private long price;
    private long amount = 0;
    private long reservedAmount = 0;

    public ItemDTO() {}

    public ItemDTO(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.price = item.getPrice();
        this.amount = item.getAmount();
        this.reservedAmount = item.getReservedAmount();
    }

    public long getId() {
        return this.id;
    }

    private void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public long getPrice() {
        return this.price;
    }

    private void setPrice(long price) {
        this.price = price;
    }
    
    public long getAmount() {
        return this.amount;
    }

    private void setAmount(long amount) {
        this.amount = amount;
    }

    public long getReservedAmount() {
        return this.reservedAmount;
    }

    private void setReservedAmount(long reservedAmount) {
        this.reservedAmount = reservedAmount;
    }
}
