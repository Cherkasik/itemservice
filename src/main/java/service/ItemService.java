package service;

import dao.*;
import dto.*;
import entity.*;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Logger;

public class ItemService {
    private ItemDAO itemDAO;
    private ItemWarehouseDAO itemWarehouseDAO;
    private Logger logger;

    public ItemService(ItemDAO itemDAO, ItemWarehouseDAO itemWarehouseDAO, Logger logger) {
        this.itemDAO = itemDAO;
        this.itemWarehouseDAO = itemWarehouseDAO;
        this.logger = logger;
    }

    public ItemDTO createItem(ItemDTO itemAdditionDTO) {
        Item item = new Item(itemAdditionDTO);
        ItemWarehouse itemWarehouse = new ItemWarehouse(item);
        itemDAO.save(item);
        itemWarehouseDAO.save(itemWarehouse);
        logger.info("Created item " + itemAdditionDTO.getName());
        return new ItemDTO(item);
    }

    public ItemDTO getItemDTOById(long itemId) {
        Item item = itemDAO.getItemById(itemId);
        if (item != null) {
            ItemWarehouse itemWarehouse = itemWarehouseDAO.getItemWarehouseByItemId(item.getId());
            return new ItemDTO(item, itemWarehouse.getAmount() - itemWarehouse.getReservedAmount());
        }
        throw new NullPointerException("Cannot find element by this id");
    }

    public List<ItemDTO> getItems() {
        List<Item> items = itemDAO.getItems();
        List<ItemDTO> itemsDTO;
        if (items == null) throw new NullPointerException("Cannot find element by this id");
        return items.stream().map(item -> {
            ItemWarehouse itemWarehouse = itemWarehouseDAO.getItemWarehouseByItemId(item.getId());
            return new ItemDTO(item, itemWarehouse.getAmount() - itemWarehouse.getReservedAmount());
        }).collect(Collectors.toList());
    }

    public ItemDTO addExistingItems(long itemId, long amount) {
        Item item = itemDAO.getItemById(itemId);
        ItemWarehouse itemWarehouse = itemWarehouseDAO.getItemWarehouseByItemId(itemId);
        if (amount > 0) {
            itemWarehouse.changeAmount(amount);
            itemWarehouseDAO.update(itemWarehouse);
            logger.info("Added " + amount + " items for " + item.getName());
        } else {
            logger.error("Cannot add number below zero");        }
        return new ItemDTO(item, itemWarehouse.getAmount() - itemWarehouse.getReservedAmount());
    }

    public ItemDTO changeItemAmount(long itemId, long amount) {
        Item item = itemDAO.getItemById(itemId);
        ItemWarehouse itemWarehouse = itemWarehouseDAO.getItemWarehouseByItemId(itemId);
        if (amount > 0) {
            itemWarehouse.changeAmount(amount);
            itemWarehouseDAO.update(itemWarehouse);
            logger.info("Added " + amount + " items for " + item.getName());
        } else if (amount < 0 && itemWarehouse.getAmount() <= Math.abs(amount)) {
            itemWarehouse.changeAmount(amount);
            itemWarehouseDAO.update(itemWarehouse);
            logger.info("Deleted " + Math.abs(amount) + " items for " + item.getName());
        } else {
            logger.info("Nothing was added");
        }
        return new ItemDTO(item, itemWarehouse.getAmount() - itemWarehouse.getReservedAmount());
    }

    public boolean reserveItems(long itemId, long amount) {
        Item item = itemDAO.getItemById(itemId);
        ItemWarehouse itemWarehouse = itemWarehouseDAO.getItemWarehouseByItemId(itemId);
        if (amount > 0 && itemWarehouse.getAmount() - itemWarehouse.getReservedAmount() >= amount) {
            itemWarehouse.changeReservedAmount(amount);
            itemWarehouseDAO.update(itemWarehouse);
            logger.info("Reserved " + amount + " items for " + item.getName());
            return true;
        }
        logger.info("Nothing was reserved");
        return false;
    }

    public boolean releaseItems(long itemId, long amount) {
        Item item = itemDAO.getItemById(itemId);
        ItemWarehouse itemWarehouse = itemWarehouseDAO.getItemWarehouseByItemId(itemId);
        if (amount > 0) {
            itemWarehouse.changeReservedAmount(amount);
            itemWarehouseDAO.update(itemWarehouse);
            logger.info("Released " + amount + " items for " + item.getName());
            return true;
        }
        logger.info("Nothing was released");
        return false;
    }
}
