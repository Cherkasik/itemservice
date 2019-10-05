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
		get("/api/warehouse/items", (req, res) -> itemService.getItems());

        // get item by id
        get("/api/warehouse/items/:itemId", (req, res) ->
            itemService.getItemDTOById(Long.parseLong(req.params("itemId")))
        );

        // create new item
		post("/api/warehouse/items", (req, res) ->
			itemService.createItem(new Gson().fromJson(req.body(), ItemDTO.class))
		);

        // add existing items
        put("api/warehouse/items/:itemId/addition/:amount", (req, res) ->
            itemService.addExistingItems(Long.parseLong(req.params("itemId")), Long.parseLong(req.params("amount")))
        );

        // change amount of items
		post("api/warehouse/items/:itemId/change/:amount", (req, res) ->
            itemService.changeItemAmount(Long.parseLong(req.params("itemId")), Long.parseLong(req.params("amount")))
        );

        // reserve items
        post("api/warehouse/items/:itemId/reserve/:amount", (req, res) -> 
            itemService.reserveItems(Long.parseLong(req.params("itemId")), Long.parseLong(req.params("amount")))
        );

        // release items
        post("api/warehouse/items/:itemId/release/:amount", (req, res) ->
            itemService.releaseItems(Long.parseLong(req.params("itemId")), Long.parseLong(req.params("amount")))
        );
    }

	private static Long parseLong(String s) {
		if (s == null || s.equals("") || s.toLowerCase().equals("null")) {
			return null;
		}
		return Long.parseLong(s);
	}
}
