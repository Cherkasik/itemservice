package dto;

import entity.ItemWarehouse;
import java.util.stream.Collectors;

public class ItemWarehouseDTO {
    private long id;
    private long itemId;
    private long amount = 0;
    private long reservedAmount = 0;

    public ItemWarehouseDTO() {}

    public ItemWarehouseDTO(ItemWarehouse itemWarehouse) {
        this.id = itemWarehouse.getId();
        this.itemId = itemWarehouse.getItemId();
        this.amount = itemWarehouse.getAmount();
        this.reservedAmount = itemWarehouse.getAmount();
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getItemId() {
        return this.itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
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