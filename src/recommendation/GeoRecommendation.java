package recommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

public class GeoRecommendation {
	public List<Item> recommendItems(String userId, double lat, double lon, Integer radius) {
		// 1.Get all favorite item ids.
		DBConnection dbConnection = DBConnectionFactory.getConnection();
		Set<String> favoritedItemIds = dbConnection.getFavoriteItemIds(userId);

		// 2.Get all categories, sort by count.
		// {"sports": 5, "music": 3, "art": 2}
		Map<String, Integer> allCategories = new HashMap<>();
		for (String itemId : favoritedItemIds) {
			Set<String> categories = dbConnection.getCategories(itemId);
			for (String category : categories) {
				allCategories.put(category, allCategories.getOrDefault(category, 0) + 1);
			}
		}
		List<Entry<String, Integer>> categoryList = new ArrayList<>(allCategories.entrySet());
		Collections.sort(categoryList, (Entry<String, Integer> e1, Entry<String, Integer> e2) -> {
			return Integer.compare(e2.getValue(), e1.getValue());
		});

		// 3.Search based on category, filter out favorite items and visited items.
		Set<String> visitedItemIds = new HashSet<>();
		List<Item> recommendedItems = new ArrayList<>();
		for (Entry<String, Integer> category : categoryList) {
			List<Item> items = dbConnection.searchItems(lat, lon, category.getKey(), radius);
			for (Item item : items) {
				if (!favoritedItemIds.contains(item.getItemId()) && !visitedItemIds.contains(item.getItemId())) {
					recommendedItems.add(item);
					visitedItemIds.add(item.getItemId());
				}
			}
		}

		dbConnection.close();
		return recommendedItems;
	}

}
