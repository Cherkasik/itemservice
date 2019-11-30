package service;

import dao.*;
import dto.*;
import entity.*;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import com.google.gson.Gson;

public class ItemService {
    private ItemDAO itemDAO;
    private ItemWarehouseDAO itemWarehouseDAO;
    private MessagingService messagingService;
    private static final Logger logger = LogManager.getLogger(ItemService.class);

    public ItemService(ItemDAO itemDAO, ItemWarehouseDAO itemWarehouseDAO, MessagingService messagingService) {
        this.itemDAO = itemDAO;
        this.itemWarehouseDAO = itemWarehouseDAO;
        this.messagingService = messagingService;
    }

    public String createItem(ItemDTO itemAdditionDTO) {
        Item item = new Item(itemAdditionDTO);
        ItemWarehouse itemWarehouse = new ItemWarehouse(itemAdditionDTO);
        itemDAO.save(item);
        itemWarehouseDAO.save(itemWarehouse);
        logger.info("Created item " + itemAdditionDTO.getName());

        Gson gson = new Gson();
        String json = gson.toJson(new ItemDTO(item, itemWarehouse.getAmount()));
        return json;
    }

    public String getItemDTOById(long itemId) throws Throwable {
        Item item = itemDAO.getItemById(itemId);
        if (item != null) {
            ItemWarehouse itemWarehouse = itemWarehouseDAO.getItemWarehouseByItemId(item.getId());
            Gson gson = new Gson();
            String json = gson.toJson(new ItemDTO(item, itemWarehouse.getAmount() - itemWarehouse.getReservedAmount()));
            return json;
        }
        throw new Exception("Cannot find element by this id");
    }

    public String getItems() {
        List<Item> items = itemDAO.getItems();
        Gson gson = new Gson();
        String json = gson.toJson(items.stream().map(item -> {
            ItemWarehouse itemWarehouse = itemWarehouseDAO.getItemWarehouseByItemId(item.getId());
            return new ItemDTO(item, itemWarehouse.getAmount() - itemWarehouse.getReservedAmount());
        }).collect(Collectors.toList()));
        return json;
    }

    public String addExistingItems(long itemId, long amount) {
        Item item = itemDAO.getItemById(itemId);
        ItemWarehouse itemWarehouse = itemWarehouseDAO.getItemWarehouseByItemId(itemId);
        if (amount > 0) {
            itemWarehouse.changeAmount(amount);
            itemWarehouseDAO.update(itemWarehouse);
            logger.info("Added " + amount + " items for " + item.getName());
        } else {
            logger.error("Cannot add number below zero");
        }
        Gson gson = new Gson();
        String json = gson.toJson(new ItemDTO(item, itemWarehouse.getAmount() - itemWarehouse.getReservedAmount()));
        return json;
    }

    public String changeItemAmount(long itemId, long amount, Long orderId) {
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
            } else if (amount < 0 && itemWarehouse.getAmount() >= Math.abs(amount)) {
                itemWarehouse.changeAmount(amount);
                itemWarehouseDAO.update(itemWarehouse);
                logger.info("Deleted " + Math.abs(amount) + " items for " + item.getName());
            } else {
                logger.info("Nothing was added");
            }
            Gson gson = new Gson();
            String json = gson.toJson(new ItemDTO(item, itemWarehouse.getAmount() - itemWarehouse.getReservedAmount()));
            return json;
        } catch (Throwable e) {
            logger.error(e.getMessage());
            try {
                messagingService.broadcastResponse(itemId, "changingAmountFailed", amount, orderId);
            }
            catch (Exception err) {
                logger.error(err.getMessage());
            }

            Gson gson = new Gson();
            String json = gson.toJson(new ItemDTO(itemDAO.getItemById(itemId), amount));
            return json;
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
            messagingService.broadcastResponse(itemId, "reservationFailed", amount, orderId);
            return false;
        } catch (Throwable e) {
            logger.error(e.getMessage());
            try {
                messagingService.broadcastResponse(itemId, "reservationFailed", amount, orderId);
            } catch (Exception err) {
                logger.error(err.getMessage());
            }
            return false;
        }
    }

    public boolean releaseItems(long itemId, long amount, Long orderId) {
        try {
            Item item = itemDAO.getItemById(itemId);
            ItemWarehouse itemWarehouse = itemWarehouseDAO.getItemWarehouseByItemId(itemId);
            if (amount > 0) {
                itemWarehouse.changeReservedAmount(-amount);
                itemWarehouseDAO.update(itemWarehouse);
                logger.info("Released " + amount + " items for " + item.getName());
                return true;
            }
            logger.info("Nothing was released");
            messagingService.broadcastResponse(itemId, "releasingFailed", amount, orderId);
            return false;
        } catch (Throwable e) {
            logger.error(e.getMessage());
            try {
                messagingService.broadcastResponse(itemId, "releasingFailed", amount, orderId);
            } catch (Exception err) {
                logger.error(err.getMessage());
            }
            return false;
        }
    }
}
