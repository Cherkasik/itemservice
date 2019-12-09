package service;

import com.google.gson.JsonObject;
import com.rabbitmq.client.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import com.google.gson.Gson;

import dto.*;
import entity.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MessagingService {
    private static final Logger logger = LogManager.getLogger(MessagingService.class);

    public void setupListener(ItemService itemService) {
        String EXCHANGE_NAME_CHANGE = "changeItemAmount";
        String EXCHANGE_NAME_RELEASE = "itemRemovedFromOrder";
        String EXCHANGE_NAME_RESERVE = "itemAddedToOrder";
        String EXCHANGE_NAME_BOUGHT = "itemBought";
        String QUEUE_NAME = "ItemService";

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection;
        Channel channel;

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME_CHANGE, "direct");
            channel.exchangeDeclare(EXCHANGE_NAME_RELEASE, "fanout");
            channel.exchangeDeclare(EXCHANGE_NAME_RESERVE, "fanout");
            channel.exchangeDeclare(EXCHANGE_NAME_BOUGHT, "fanout");

            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME_CHANGE, "ItemService");
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME_RELEASE, "");
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME_RESERVE, "");
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME_BOUGHT, "");

            logger.info(QUEUE_NAME + " waiting for messages");

            DeliverCallback driverCallback = (consumerTag, delivery) -> {
                try {
                    String message = new String(delivery.getBody(), "UTF-8");
                    logger.info(QUEUE_NAME + " received '" + message + "'");
                    System.out.println(QUEUE_NAME + " received '" + message + "'");
                    ItemAmountDTO dto = new Gson().fromJson(message, ItemAmountDTO.class);

                    if (dto.getType().equals(EXCHANGE_NAME_CHANGE)) {
                        itemService.changeItemAmount(dto.getId(), dto.getAmount(), dto.getOrderId());
                    };
                    if (dto.getType().equals(EXCHANGE_NAME_RESERVE)) {
                        itemService.reserveItems(dto.getId(), dto.getAmount(), dto.getOrderId());
                    };
                    if (dto.getType().equals(EXCHANGE_NAME_RELEASE)) {
                        itemService.releaseItems(dto.getId(), dto.getAmount(), dto.getOrderId());
                    };
                    if (dto.getType().equals(EXCHANGE_NAME_BOUGHT)) {
                        itemService.changeItemAmount(dto.getId(), -dto.getAmount(), dto.getOrderId());
                    };
                } finally {
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                };
            };

            channel.basicConsume(QUEUE_NAME, false, driverCallback, consumerTag -> { });
        } catch(Exception e) {
            logger.error("Failed to setupListener of itemService to listen to requests to it");
        };
    };

    public void broadcastResponse (long itemId, String exchangeName, long amount, Long orderId) throws Exception {
        logger.info("Response with itemId - " + itemId + ", exchangeName - " + exchangeName + " amount - " + amount + ", orderId - " + orderId);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(exchangeName, "fanout");

            JsonObject json = new JsonObject();
            json.addProperty("type", exchangeName);
            json.addProperty("itemId", itemId);
            json.addProperty("amount", amount);
            json.addProperty("orderId", orderId);

            String message = json.toString();

            channel.basicPublish(exchangeName, "", MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
            logger.info("[Item Service] Sent '" + message + "'");
        }
    }

    public void broadcastResponseItemAdded (long itemId, String itemName, long price) throws Exception {
        logger.info("Item added with itemId - " + itemId + ", name - " + itemName + " price - " + price);
        String exchangeName = "itemAddedToWarehouse";
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(exchangeName, "fanout");

            JsonObject json = new JsonObject();
            json.addProperty("type", exchangeName);
            json.addProperty("id", itemId);
            json.addProperty("name", itemName);
            json.addProperty("price", price);

            String message = json.toString();

            channel.basicPublish(exchangeName, "", MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
            logger.info("[Item Service] Sent '" + message + "'");
        }
    }
}
