package service;

import com.google.gson.JsonObject;
import com.rabbitmq.client.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import com.google.gson.Gson;

import dto.*;
import entity.*;

public class MessagingService {
    private static final Logger logger = LoggerFactory.getLogger(MessagingService.class);

    private static final String EXCHANGE_NAME_CHANGE = "changeItemAmount";
    private static final String EXCHANGE_NAME_RELEASE = "reserveItems";
    private static final String EXCHANGE_NAME_RESERVE = "releaseItems";
    private static final String QUEUE_NAME = "ItemService";

    public static void setupListener(ItemService itemService) {
        String queueName = "ItemService";

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
                    Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME_CHANGE, "direct");

            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME_CHANGE, "ItemService");
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME_RELEASE, "ItemService");
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME_RESERVE, "ItemService");

            logger.info(QUEUE_NAME + " waiting for messages");

            DeliverCallback changeCallback = (consumerTag, delivery) -> {
                try {
                    String message = new String(delivery.getBody(), "UTF-8");
                    logger.info(QUEUE_NAME + " received '" + message + "'");
                    ItemAmountDTO dto = new Gson().fromJson(message, ItemAmountDTO.class);
                    if (message.type == EXCHANGE_NAME_CHANGE) {
                        itemService.ChangeItemAmount(dto.getId(), dto.getAmount(), dto.getOrderId());
                    }
                    if (message.type == EXCHANGE_NAME_RESERVE) {
                        itemService.reserveItems(dto.getId(), dto.getAmount(), dto.getOrderId());
                    }
                    if (message.type == EXCHANGE_NAME_RELEASE) {
                        itemService.releaseItems(dto.getId(), dto.getAmount(), dto.getOrderId());
                    }
                } finally {
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };

            channel.basicConsume(QUEUE_NAME, false, driverCallback, consumerTag -> { });
        } catch(Exception e) {
            logger.error("Failed to setupListener of itemService to listen to requests to it");
        }
    }

    public void broadcastResponce (boolean itemAmountChanged, long itemId, String exchangeName, long amount, long orderId) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(exchangeName, "fanout");

            JsonObject json = new JsonObject();
            json.addProperty("type", exchangeName);
            json.addProperty(exchangeName, Boolean.toString(itemAmountChanged));
            json.addProperty("itemId", itemId);
            json.addProperty("amount", amount);
            json.addProperty("orderId", orderId);

            String message = json.toString();

            channel.basicPublish(exchangeName, "", MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
            logger.info("[Item Service] Sent '" + message + "'");
        }
    }
}