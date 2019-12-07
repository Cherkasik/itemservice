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
    private MessagingService messagingService;
    private static final Logger logger = LogManager.getLogger(ItemService.class);

    public ItemService(ItemDAO itemDAO, MessagingService messagingService) {
        this.itemDAO = itemDAO;
        this.messagingService = messagingService;
    }

    public String createItem(ItemDTO itemAdditionDTO) {
        Item item = new Item(itemAdditionDTO);
        itemDAO.save(item);
        logger.info("Created item " + itemAdditionDTO.getName());

        Gson gson = new Gson();
        String json = gson.toJson(new ItemDTO(item));
        return json;
    }

    public String getItemDTOById(long itemId) throws Throwable {
        Item item = itemDAO.getItemById(itemId);
        if (item != null) {
            Gson gson = new Gson();
            String json = gson.toJson(new ItemDTO(item));
            return json;
        }
        throw new Exception("Cannot find element by this id");
    }

    public String getItems() {
        List<Item> items = itemDAO.getItems();
        Gson gson = new Gson();
        String json = gson.toJson(items.stream().map(item -> {
            return new ItemDTO(item);
        }).collect(Collectors.toList()));
        return json;
    }

    public String addExistingItems(long itemId, long amount) {
        Item item = itemDAO.getItemById(itemId);
        if (amount > 0) {
            item.changeAmount(amount);
            itemDAO.update(item);
            logger.info("Added " + amount + " items for " + item.getName());
        } else {
            logger.error("Cannot add number below zero");
        }
        Gson gson = new Gson();
        String json = gson.toJson(new ItemDTO(item));
        return json;
    }

    public String changeItemAmount(long itemId, long amount, Long orderId) {
        try {
            Item item = itemDAO.getItemById(itemId);
            logger.info("Changing item amount for item: " + item);
            logger.info("amount: " + amount);
            logger.info("orderId: " + orderId);
            if (amount > 0) {
                item.changeAmount(amount);
                itemDAO.update(item);
                logger.info("Added " + amount + " items for " + item.getName());
            } else if (amount < 0 && item.getAmount() >= Math.abs(amount)) {
                item.changeAmount(amount);
                itemDAO.update(item);
                logger.info("Deleted " + Math.abs(amount) + " items for " + item.getName());
            } else {
                logger.info("Nothing was added");
            }
            Gson gson = new Gson();
            String json = gson.toJson(new ItemDTO(item));
            return json;
        } catch (Throwable e) {
            logger.info("Changing item amount for item failed");
            logger.error(e.getMessage());
            try {
                messagingService.broadcastResponse(itemId, "changingAmountFailed", amount, orderId);
            }
            catch (Exception err) {
                logger.error(err.getMessage());
            }

            Gson gson = new Gson();
            String json = gson.toJson(new ItemDTO(itemDAO.getItemById(itemId)));
            return json;
        }
    }

    public String reserveItems(long itemId, long amount, Long orderId) {
        try {
            Item item = itemDAO.getItemById(itemId);
            logger.info("Reserving items for item: " + item);
            logger.info("amount: " + amount);
            logger.info("orderId: " + orderId);
            if (amount > 0 && item.getAmount() - item.getReservedAmount() >= amount) {
                item.changeReservedAmount(amount);
                itemDAO.update(item);
                logger.info("Reserved " + amount + " items for " + item.getName());
                Gson gson = new Gson();
                String json = gson.toJson(true);
                return json;
            }
            logger.info("Nothing was reserved");
            messagingService.broadcastResponse(itemId, "reservationFailed", amount, orderId);
            Gson gson = new Gson();
            String json = gson.toJson(false);
            return json;
        } catch (Throwable e) {
            logger.info("Error during reservation");
            logger.error(e.getMessage());
            try {
                messagingService.broadcastResponse(itemId, "reservationFailed", amount, orderId);
            } catch (Exception err) {
                logger.error(err.getMessage());
            }
            Gson gson = new Gson();
            String json = gson.toJson(false);
            return json;
        }
    }

    public String releaseItems(long itemId, long amount, Long orderId) {
        try {
            Item item = itemDAO.getItemById(itemId);
            if (amount > 0) {
                item.changeReservedAmount(-amount);
                itemDAO.update(item);
                logger.info("Released " + amount + " items for " + item.getName());
                Gson gson = new Gson();
                String json = gson.toJson(true);
                return json;
            }
            logger.info("Nothing was released");
            messagingService.broadcastResponse(itemId, "releasingFailed", amount, orderId);
            Gson gson = new Gson();
            String json = gson.toJson(false);
            return json;
        } catch (Throwable e) {
            logger.info("Releasing was failed");
            logger.error(e.getMessage());
            try {
                messagingService.broadcastResponse(itemId, "releasingFailed", amount, orderId);
            } catch (Exception err) {
                logger.error(err.getMessage());
            }
            Gson gson = new Gson();
            String json = gson.toJson(false);
            return json;
        }
    }
}
