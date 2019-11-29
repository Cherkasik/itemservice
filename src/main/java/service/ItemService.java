package service;

import dao.*;
import dto.*;
import entity.*;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ItemService {
    private ItemDAO itemDAO;
    private ItemWarehouseDAO itemWarehouseDAO;
    private static final Logger logger = LogManager.getLogger(ItemService.class);

    public ItemService(ItemDAO itemDAO, ItemWarehouseDAO itemWarehouseDAO) {
        this.itemDAO = itemDAO;
        this.itemWarehouseDAO = itemWarehouseDAO;
    }

    public ItemDTO createItem(ItemDTO itemAdditionDTO) {
        Item item = new Item(itemAdditionDTO);
        ItemWarehouse itemWarehouse = new ItemWarehouse(item);
        itemDAO.save(item);
        itemWarehouseDAO.save(itemWarehouse);
        logger.info("Created item " + itemAdditionDTO.getName());
        return new ItemDTO(item);
    }

    public ItemDTO getItemDTOById(long itemId) throws Throwable {
        Item item = itemDAO.getItemById(itemId);
        if (item != null) {
            ItemWarehouse itemWarehouse = itemWarehouseDAO.getItemWarehouseByItemId(item.getId());
            return new ItemDTO(item, itemWarehouse.getAmount() - itemWarehouse.getReservedAmount());
        }
        throw new Exception("Cannot find element by this id");
    }

    public List<ItemDTO> getItems() {
        List<Item> items = itemDAO.getItems();
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

    public ItemDTO changeItemAmount(long itemId, long amount, Long orderId) {
        try {
            Item item = itemDAO.getItemById(itemId);
            System.out.println("item: " + item);
            ItemWarehouse itemWarehouse = itemWarehouseDAO.getItemWarehouseByItemId(itemId);
            System.out.println("itemWarehouse: " + itemWarehouse);
            System.out.println("amount: " + amount);
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
        } catch (Throwable e) {
            logger.error(e.getMessage());
            try {
                MessagingService.broadcastResponse(itemId, "changingAmountFailed", amount, orderId);
            }
            catch (Exception err) {
                logger.error(err.getMessage());
            }
            return new ItemDTO(itemDAO.getItemById(itemId), amount);
        }
    }

    public boolean reserveItems(long itemId, long amount, Long orderId) {
        try {
            Item item = itemDAO.getItemById(itemId);
            ItemWarehouse itemWarehouse = itemWarehouseDAO.getItemWarehouseByItemId(itemId);
            if (amount > 0 && itemWarehouse.getAmount() - itemWarehouse.getReservedAmount() >= amount) {
                itemWarehouse.changeReservedAmount(amount);
                itemWarehouseDAO.update(itemWarehouse);
                logger.info("Reserved " + amount + " items for " + item.getName());
                return true;
            }
            logger.info("Nothing was reserved");
            MessagingService.broadcastResponse(itemId, "reservationFailed", amount, orderId);
            return false;
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    public boolean releaseItems(long itemId, long amount, Long orderId) {
        try {
            Item item = itemDAO.getItemById(itemId);
            ItemWarehouse itemWarehouse = itemWarehouseDAO.getItemWarehouseByItemId(itemId);
            if (amount > 0) {
                itemWarehouse.changeReservedAmount(amount);
                itemWarehouseDAO.update(itemWarehouse);
                logger.info("Released " + amount + " items for " + item.getName());
                return true;
            }
            logger.info("Nothing was released");
            MessagingService.broadcastResponse(itemId, "releasingFailed", amount, orderId);
            return false;
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return false;
        }
    }
}
