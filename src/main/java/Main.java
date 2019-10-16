import static spark.Spark.*;

import com.google.gson.Gson;
import dao.ItemDAO;
import entity.Item;
import dto.ItemDTO;
import service.ItemService;

public class Main {
	private static ItemDAO orderDAO = new ItemDAO();
	private static ItemService itemService = new ItemService();

    public static void main(String[] args) {
    	port(1824);

		exception(Exception.class, (exception, request, response) -> exception.printStackTrace());

        // get items
		get("/api/warehouse/items", (req, res) -> {
            try {
                return itemService.getItems();
            } catch (NullPointerException e) {
                // TODO: replace for  logger
                e.printStackTrace();
            }
        });

        // get item by id
        get("/api/warehouse/items/:itemId", (req, res) ->
            itemService.getItemDTOById(Long.parseLong(req.params("itemId")))
        );

        // create new item
		post("/api/warehouse/items", (req, res) ->
			itemService.createItem(new Gson().fromJson(req.body(), ItemDTO.class))
		);

        // add existing items
        put("api/warehouse/items/:itemId/addition/:amount", (req, res) -> {
            try {
                return itemService.addExistingItems(Long.parseLong(req.params("itemId")), Long.parseLong(req.params("amount")));
            } catch (NullPointerException e) {
                //TODO: replace for logger
                e.printStackTrace();
            }
        }
        );

        // change amount of items
		post("api/warehouse/items/:itemId/change/:amount", (req, res) ->{
            try {
                return itemService.changeItemAmount(Long.parseLong(req.params("itemId")), Long.parseLong(req.params("amount")));
            } catch (NullPointerException e) {
                //TODO: replace for logger
                e.printStackTrace();
            }
        }
        );

        // reserve items
        post("api/warehouse/items/:itemId/reserve/:amount", (req, res) -> {
            try {
                return itemService.reserveItems(Long.parseLong(req.params("itemId")), Long.parseLong(req.params("amount")));
            } catch (NullPointerException e) {
                //TODO: replace for logger
                e.printStackTrace();
            }
        }
        );

        // release items
        post("api/warehouse/items/:itemId/release/:amount", (req, res) -> {
            try {
                return itemService.releaseItems(Long.parseLong(req.params("itemId")), Long.parseLong(req.params("amount")));
            } catch (NullPointerException e) {
                //TODO: replace for logger
                e.printStackTrace();
            }
        }
        );
    }

	private static Long parseLong(String s) {
		if (s == null || s.equals("") || s.toLowerCase().equals("null")) {
			throw new NullPointerException('Value is empty or null');
		}
		return Long.parseLong(s);
	}
}
