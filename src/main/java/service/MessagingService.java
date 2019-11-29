package service;

import com.google.gson.JsonObject;
import com.rabbitmq.client.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import com.google.gson.Gson;

import dto.*;
import entity.*;

public class MessagingService {
    private static final Logger logger = LogManager.getLogger(MessagingService.class);

    public static void setupListener(ItemService itemService) {
        String EXCHANGE_NAME_CHANGE = "changeItemAmount";
        String EXCHANGE_NAME_RELEASE = "reserveItems";
        String EXCHANGE_NAME_RESERVE = "releaseItems";
        String QUEUE_NAME = "ItemService";

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
                    Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME_CHANGE, "direct");
            channel.exchangeDeclare(EXCHANGE_NAME_RELEASE, "direct");
            channel.exchangeDeclare(EXCHANGE_NAME_RESERVE, "direct");

            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME_CHANGE, "ItemService");
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME_RELEASE, "ItemService");
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME_RESERVE, "ItemService");

            logger.info(QUEUE_NAME + " waiting for messages");

            DeliverCallback driverCallback = (consumerTag, delivery) -> {
                try {
                    String message = new String(delivery.getBody(), "UTF-8");
                    logger.info(QUEUE_NAME + " received '" + message + "'");
                    ItemAmountDTO dto = new Gson().fromJson(message, ItemAmountDTO.class);
                    System.out.println("dto type: " + dto.getType() + ".");
                    System.out.println("exchange type: " + EXCHANGE_NAME_CHANGE + ".");
                    System.out.println("==?: " + (dto.getType() == EXCHANGE_NAME_CHANGE));
                    System.out.println("equals?: " + dto.getType().equals(EXCHANGE_NAME_CHANGE));
                    if (dto.getType().equals(EXCHANGE_NAME_CHANGE)) {
                        System.out.println("trying to change amount");
                        System.out.println("long: " + (long)1);
                        System.out.println("dto.getAmount(): " + dto.getAmount());
                        System.out.println("==?: " + (dto.getAmount() == (long)1)); 
                        System.out.println("dto.getId(): " + dto.getId());
                        itemService.changeItemAmount(dto.getId(), dto.getAmount(), dto.getOrderId());
                    };
                    if (dto.getType().equals(EXCHANGE_NAME_RESERVE)) {
                        itemService.reserveItems(dto.getId(), dto.getAmount(), dto.getOrderId());
                    };
                    if (dto.getType().equals(EXCHANGE_NAME_RELEASE)) {
                        itemService.releaseItems(dto.getId(), dto.getAmount(), dto.getOrderId());
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

    public static void broadcastResponse (long itemId, String exchangeName, long amount, Long orderId) throws Exception {
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
}

/*
finally изменить на что-то
Переполучение сообщения от Rabbit
сделать все не static
одиночное объявления connection, exchangeDeclare
Все поднять и подергать через рест на паре, видеть все изменения и логи
*/
