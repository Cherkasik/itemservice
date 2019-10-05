package dto;

import entity.Item;

public class ItemDTO {
    private long id;
    private String name;
    private long price;
    private long amount = 0;

    public ItemDTO() {}

    public ItemDTO(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.price = item.getPrice();
        this.amount = 0;
    }

    public ItemDTO(Item item, long amount) {
        this.id = item.getId();
        this.name = item.getName();
        this.price = item.getPrice();
        this.amount = 0;
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
}